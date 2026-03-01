#include "llama_runner_core.h"

#include <cstring>
#include <iostream>
#include <string>

namespace {

void ios_log(LlamaLogLevel level, const char *msg) {
    if (level == LLAMA_LOG_ERROR) {
        std::cerr << "[LlamaRunner] ERROR: " << (msg ? msg : "") << std::endl;
        return;
    }
    if (level == LLAMA_LOG_WARN) {
        std::cerr << "[LlamaRunner] WARN: " << (msg ? msg : "") << std::endl;
        return;
    }
    std::cout << "[LlamaRunner] " << (msg ? msg : "") << std::endl;
}

} // namespace

extern "C" {

void llama_runner_init(void) {
    llama_runner_core_set_logger(ios_log);
    llama_runner_core_init(nullptr);
}

int llama_runner_load_model(
    const char *model_path,
    int n_ctx,
    int n_threads,
    int n_batch,
    int n_gpu_layers,
    float temperature) {
    LlamaRunnerConfig config;
    config.n_ctx = n_ctx;
    config.n_threads = n_threads;
    config.n_batch = n_batch;
    config.n_gpu_layers = n_gpu_layers;
    config.temperature = temperature;
    return llama_runner_core_load_model(model_path, config) ? 1 : 0;
}

char *llama_runner_generate_text(const char *prompt, int max_tokens, float temperature) {
    const std::string result = llama_runner_core_generate(prompt, max_tokens, temperature);
    return strdup(result.c_str());
}

void llama_runner_unload_model(void) { llama_runner_core_unload(); }

void llama_runner_shutdown(void) { llama_runner_core_shutdown(); }

}
