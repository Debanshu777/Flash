#include "llama.h"
#include "common.h"
#include "sampling.h"
#include <string>
#include <vector>
#include <cstring>
#include <iostream>
#include <chrono>

static llama_model *g_model = nullptr;
static llama_context *g_context = nullptr;
static llama_batch g_batch;
static struct common_sampler *g_sampler = nullptr;

#define LOG_I(msg) std::cout << "[LlamaRunner] " << msg << std::endl
#define LOG_E(msg) std::cerr << "[LlamaRunner] ERROR: " << msg << std::endl

static std::string truncate_for_log(const std::string &s, size_t max_len = 80) {
    if (s.size() <= max_len) return s;
    return s.substr(0, max_len) + "...";
}

extern "C" {

void llama_runner_init(void) {
    LOG_I("init: Starting backend initialization");
    llama_backend_init();
    LOG_I("init: Backend initialized successfully");
}

int llama_runner_load_model(const char *model_path) {
    LOG_I("load: path=" << (model_path ? model_path : "(null)"));
    if (!model_path || strlen(model_path) == 0) {
        LOG_E("load: model_path is null or empty");
        return 0;
    }

    auto t0 = std::chrono::steady_clock::now();

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;  // Force CPU to avoid Metal backend issues on simulator
    LOG_I("load: n_gpu_layers=0 (CPU only), loading model...");

    g_model = llama_model_load_from_file(model_path, model_params);
    auto t1 = std::chrono::steady_clock::now();
    auto load_ms = std::chrono::duration_cast<std::chrono::milliseconds>(t1 - t0).count();

    if (!g_model) {
        LOG_E("load: Failed to load model from " << model_path << " (took " << load_ms << "ms)");
        return 0;
    }
    LOG_I("load: Model loaded in " << load_ms << "ms");

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 2048;
    ctx_params.n_threads = 4;
    ctx_params.n_batch = 512;
    LOG_I("load: Creating context (n_ctx=2048, n_threads=4, n_batch=512)...");

    g_context = llama_init_from_model(g_model, ctx_params);
    if (!g_context) {
        LOG_E("load: Failed to create context");
        llama_model_free(g_model);
        g_model = nullptr;
        return 0;
    }
    LOG_I("load: Context created, n_ctx=" << llama_n_ctx(g_context));

    g_batch = llama_batch_init(512, 0, 1);

    struct common_params_sampling sparams;
    sparams.temp = 0.3f;
    g_sampler = common_sampler_init(g_model, sparams);

    LOG_I("load: Model ready (vocab_size=" << llama_vocab_n_tokens(llama_model_get_vocab(g_model))
                                           << ")");
    return 1;
}

char *llama_runner_generate_text(const char *prompt, int max_tokens) {
    LOG_I("generate: entry (max_tokens=" << max_tokens << ")");

    if (!g_model || !g_context || !g_sampler) {
        LOG_E("generate: Model not loaded");
        return strdup("");
    }

    if (!prompt || strlen(prompt) == 0) {
        LOG_E("generate: Empty prompt");
        return strdup("");
    }

    if (max_tokens <= 0) {
        LOG_E("generate: max_tokens must be > 0");
        return strdup("");
    }

    llama_memory_clear(llama_get_memory(g_context), false);

    std::string promptStr(prompt);
    LOG_I("generate: input prompt_len=" << promptStr.size() << " preview=\""
                                        << truncate_for_log(promptStr) << "\"");

    const llama_vocab *vocab = llama_model_get_vocab(g_model);
    std::vector<llama_token> tokens = common_tokenize(g_context, promptStr, true, true);
    LOG_I("generate: tokenized n_tokens=" << tokens.size());

    if (tokens.empty()) {
        LOG_E("generate: Tokenization produced no tokens");
        return strdup("");
    }

    auto t0 = std::chrono::steady_clock::now();
    std::string result;

    // Encode prompt - request logits only for the last token (needed for sampling)
    common_batch_clear(g_batch);
    for (size_t i = 0; i < tokens.size(); i++) {
        bool request_logits = (i == tokens.size() - 1);
        common_batch_add(g_batch, tokens[i], (llama_pos) i, {0}, request_logits);
    }
    int decode_ret = llama_decode(g_context, g_batch);
    if (decode_ret != 0) {
        LOG_E("generate: Prefill decode failed, ret=" << decode_ret);
        return strdup("");
    }
    LOG_I("generate: Prefill decode ok");

    llama_synchronize(g_context);
    if (llama_get_logits_ith(g_context, -1) == nullptr) {
        LOG_E("generate: n_outputs=0 after prefill, aborting");
        return strdup("");
    }

    int n_generated = 0;
    for (int i = 0; i < max_tokens; i++) {
        llama_synchronize(g_context);
        if (llama_get_logits_ith(g_context, -1) == nullptr) {
            LOG_E("generate: n_outputs=0 at token " << i << ", stopping");
            break;
        }
        llama_token token = common_sampler_sample(g_sampler, g_context, -1);

        if (llama_vocab_is_eog(vocab, token)) {
            LOG_I("generate: EOG at token " << i);
            break;
        }

        char buf[128];
        int n = llama_token_to_piece(vocab, token, buf, sizeof(buf), 0, true);
        if (n > 0) {
            result.append(buf, n);
        }
        n_generated++;

        common_sampler_accept(g_sampler, token, true);

        common_batch_clear(g_batch);
        common_batch_add(g_batch, token, (llama_pos)(tokens.size() + i), {0}, true);
        decode_ret = llama_decode(g_context, g_batch);
        if (decode_ret != 0) {
            LOG_E("generate: Decode failed at token " << i << ", ret=" << decode_ret);
            break;
        }
    }

    auto t1 = std::chrono::steady_clock::now();
    auto elapsed_ms = std::chrono::duration_cast<std::chrono::milliseconds>(t1 - t0).count();
    LOG_I("generate: done n_tokens=" << n_generated << " output_len=" << result.size()
                                     << " elapsed_ms=" << elapsed_ms << " preview=\""
                                     << truncate_for_log(result) << "\"");

    return strdup(result.c_str());
}

void llama_runner_unload_model(void) {
    LOG_I("unload: Releasing model and context");

    if (g_sampler) {
        common_sampler_free(g_sampler);
        g_sampler = nullptr;
    }

    if (g_batch.n_tokens > 0) {
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

    LOG_I("unload: Model unloaded");
}

void llama_runner_shutdown(void) {
    LOG_I("shutdown: Freeing backend");
    llama_backend_free();
}

}
