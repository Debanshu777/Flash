#include "llama_runner_core.h"

#include <chrono>
#include <cstdarg>
#include <cstdio>
#include <cstring>
#include <string>
#include <vector>

#include "common.h"
#include "ggml.h"
#include "llama.h"
#include "sampling.h"

namespace {

llama_model *g_model = nullptr;
llama_context *g_context = nullptr;
llama_batch g_batch = llama_batch_init(0, 0, 0);
common_sampler *g_sampler = nullptr;
LlamaRunnerConfig g_config;
LlamaLogFn g_logger = nullptr;
float g_active_temperature = -1.0f;

std::string truncate_for_log(const std::string &s, size_t max_len = 80) {
    if (s.size() <= max_len) {
        return s;
    }
    return s.substr(0, max_len) + "...";
}

void log_line(LlamaLogLevel level, const char *fmt, ...) {
    char buffer[1024];
    va_list args;
    va_start(args, fmt);
    vsnprintf(buffer, sizeof(buffer), fmt, args);
    va_end(args);

    if (g_logger) {
        g_logger(level, buffer);
    }
}

float resolve_temperature(float temperature) {
    if (temperature >= 0.0f) {
        return temperature;
    }
    return g_config.temperature;
}

void reset_sampler(float temperature) {
    if (g_sampler) {
        common_sampler_free(g_sampler);
        g_sampler = nullptr;
    }

    common_params_sampling sparams{};
    sparams.temp = resolve_temperature(temperature);
    g_sampler = common_sampler_init(g_model, sparams);
    g_active_temperature = sparams.temp;
}

} // namespace

void llama_runner_core_set_logger(LlamaLogFn fn) {
    g_logger = fn;
}

void llama_runner_core_init(const char *backend_path) {
    if (backend_path && std::strlen(backend_path) > 0) {
        log_line(LLAMA_LOG_INFO, "init: Loading backends from %s", backend_path);
        ggml_backend_load_all_from_path(backend_path);
    } else {
        log_line(LLAMA_LOG_INFO, "init: No backend path provided, skipping backend path load");
    }
    llama_backend_init();
    log_line(LLAMA_LOG_INFO, "init: Backend initialized");
}

bool llama_runner_core_load_model(const char *model_path, const LlamaRunnerConfig &config) {
    log_line(LLAMA_LOG_INFO, "load: path=%s", model_path ? model_path : "(null)");
    if (!model_path || std::strlen(model_path) == 0) {
        log_line(LLAMA_LOG_ERROR, "load: model_path is null or empty");
        return false;
    }

    llama_runner_core_unload();
    g_config = config;

    llama_log_set(
        [](ggml_log_level level, const char *text, void * /*user_data*/) {
            const LlamaLogLevel mapped_level =
                (level == GGML_LOG_LEVEL_CONT)
                    ? LLAMA_LOG_INFO
                    : (level >= GGML_LOG_LEVEL_ERROR)
                    ? LLAMA_LOG_ERROR
                    : (level >= GGML_LOG_LEVEL_WARN) ? LLAMA_LOG_WARN : LLAMA_LOG_INFO;
            if (g_logger) {
                g_logger(mapped_level, text ? text : "");
            }
        },
        nullptr);

    auto t0 = std::chrono::steady_clock::now();

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = g_config.n_gpu_layers;
    g_model = llama_model_load_from_file(model_path, model_params);

    auto t1 = std::chrono::steady_clock::now();
    const auto load_ms = std::chrono::duration_cast<std::chrono::milliseconds>(t1 - t0).count();
    if (!g_model) {
        log_line(LLAMA_LOG_ERROR, "load: Failed (took %lld ms)", static_cast<long long>(load_ms));
        return false;
    }
    log_line(LLAMA_LOG_INFO, "load: Model loaded in %lld ms", static_cast<long long>(load_ms));

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = g_config.n_ctx;
    ctx_params.n_threads = g_config.n_threads;
    ctx_params.n_batch = g_config.n_batch;
    log_line(
        LLAMA_LOG_INFO,
        "load: Creating context (n_ctx=%d, n_threads=%d, n_batch=%d)",
        g_config.n_ctx,
        g_config.n_threads,
        g_config.n_batch);

    g_context = llama_init_from_model(g_model, ctx_params);
    if (!g_context) {
        log_line(LLAMA_LOG_ERROR, "load: Failed to create context");
        llama_model_free(g_model);
        g_model = nullptr;
        return false;
    }
    log_line(LLAMA_LOG_INFO, "load: Context ready, n_ctx=%u", llama_n_ctx(g_context));

    g_batch = llama_batch_init(g_config.n_batch, 0, 1);
    reset_sampler(g_config.temperature);
    if (!g_sampler) {
        log_line(LLAMA_LOG_ERROR, "load: Failed to initialize sampler");
        llama_runner_core_unload();
        return false;
    }

    const llama_vocab *vocab = llama_model_get_vocab(g_model);
    log_line(LLAMA_LOG_INFO, "load: Model ready (vocab_size=%d)", llama_vocab_n_tokens(vocab));
    return true;
}

std::string llama_runner_core_generate(const char *prompt, int max_tokens, float temperature) {
    log_line(LLAMA_LOG_INFO, "generate: entry max_tokens=%d", max_tokens);

    if (!g_model || !g_context || !g_sampler) {
        log_line(LLAMA_LOG_ERROR, "generate: Model not loaded");
        return "";
    }
    if (!prompt || std::strlen(prompt) == 0) {
        log_line(LLAMA_LOG_ERROR, "generate: Empty prompt");
        return "";
    }
    if (max_tokens <= 0) {
        log_line(LLAMA_LOG_ERROR, "generate: max_tokens must be > 0");
        return "";
    }

    const float target_temp = resolve_temperature(temperature);
    if (target_temp != g_active_temperature) {
        reset_sampler(target_temp);
        if (!g_sampler) {
            log_line(LLAMA_LOG_ERROR, "generate: Failed to reconfigure sampler");
            return "";
        }
    }

    llama_memory_clear(llama_get_memory(g_context), false);

    std::string prompt_copy(prompt);
    log_line(
        LLAMA_LOG_INFO,
        "generate: input prompt_len=%zu preview=\"%s\"",
        prompt_copy.size(),
        truncate_for_log(prompt_copy).c_str());

    const llama_vocab *vocab = llama_model_get_vocab(g_model);
    std::vector<llama_token> tokens = common_tokenize(vocab, prompt_copy, true, true);
    log_line(LLAMA_LOG_INFO, "generate: tokenized n_tokens=%zu", tokens.size());
    if (tokens.empty()) {
        log_line(LLAMA_LOG_ERROR, "generate: Tokenization produced no tokens");
        return "";
    }

    auto t0 = std::chrono::steady_clock::now();
    std::string result;

    common_batch_clear(g_batch);
    for (size_t i = 0; i < tokens.size(); ++i) {
        const bool request_logits = (i == tokens.size() - 1);
        common_batch_add(g_batch, tokens[i], static_cast<llama_pos>(i), {0}, request_logits);
    }

    int decode_ret = llama_decode(g_context, g_batch);
    if (decode_ret != 0) {
        log_line(LLAMA_LOG_ERROR, "generate: Prefill decode failed ret=%d", decode_ret);
        return "";
    }
    log_line(LLAMA_LOG_INFO, "generate: Prefill ok");

    llama_synchronize(g_context);
    if (llama_get_logits_ith(g_context, -1) == nullptr) {
        log_line(LLAMA_LOG_ERROR, "generate: n_outputs=0 after prefill");
        return "";
    }

    int n_generated = 0;
    for (int i = 0; i < max_tokens; ++i) {
        llama_synchronize(g_context);
        if (llama_get_logits_ith(g_context, -1) == nullptr) {
            log_line(LLAMA_LOG_ERROR, "generate: n_outputs=0 at token %d, stopping", i);
            break;
        }

        const llama_token token = common_sampler_sample(g_sampler, g_context, -1);
        if (llama_vocab_is_eog(vocab, token)) {
            log_line(LLAMA_LOG_INFO, "generate: EOG at token %d", i);
            break;
        }

        result.append(common_token_to_piece(vocab, token, true));
        n_generated++;
        common_sampler_accept(g_sampler, token, true);

        common_batch_clear(g_batch);
        common_batch_add(
            g_batch,
            token,
            static_cast<llama_pos>(tokens.size() + static_cast<size_t>(i)),
            {0},
            true);

        decode_ret = llama_decode(g_context, g_batch);
        if (decode_ret != 0) {
            log_line(LLAMA_LOG_ERROR, "generate: Decode failed at token %d ret=%d", i, decode_ret);
            break;
        }
    }

    auto t1 = std::chrono::steady_clock::now();
    const auto elapsed_ms = std::chrono::duration_cast<std::chrono::milliseconds>(t1 - t0).count();
    log_line(
        LLAMA_LOG_INFO,
        "generate: done n_tokens=%d output_len=%zu elapsed_ms=%lld preview=\"%s\"",
        n_generated,
        result.size(),
        static_cast<long long>(elapsed_ms),
        truncate_for_log(result).c_str());

    return result;
}

void llama_runner_core_unload() {
    log_line(LLAMA_LOG_INFO, "unload: Releasing model and context");

    if (g_sampler) {
        common_sampler_free(g_sampler);
        g_sampler = nullptr;
    }

    if (g_batch.token) {
        llama_batch_free(g_batch);
        g_batch = llama_batch_init(0, 0, 0);
    }

    if (g_context) {
        llama_free(g_context);
        g_context = nullptr;
    }

    if (g_model) {
        llama_model_free(g_model);
        g_model = nullptr;
    }

    g_active_temperature = -1.0f;
    log_line(LLAMA_LOG_INFO, "unload: Model unloaded");
}

void llama_runner_core_shutdown() {
    log_line(LLAMA_LOG_INFO, "shutdown: Freeing backend");
    llama_backend_free();
}
