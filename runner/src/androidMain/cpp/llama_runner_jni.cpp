#include <jni.h>
#include <string>
#include <chrono>
#include <android/log.h>
#include "llama.h"
#include "ggml.h"
#include "common.h"
#include "sampling.h"

#define LOG_TAG "LlamaRunner"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static llama_model* g_model = nullptr;
static llama_context* g_context = nullptr;
static llama_batch g_batch;
static struct common_sampler* g_sampler = nullptr;

static std::string truncate_for_log(const std::string& s, size_t max_len = 80) {
    if (s.size() <= max_len) return s;
    return s.substr(0, max_len) + "...";
}

extern "C" JNIEXPORT void JNICALL
Java_com_debanshu777_runner_LlamaRunner_nativeInit(JNIEnv* env, jobject, jstring libDir) {
    const char* path = env->GetStringUTFChars(libDir, 0);
    LOGI("init: Loading backends from %s", path ? path : "(null)");
    ggml_backend_load_all_from_path(path);
    env->ReleaseStringUTFChars(libDir, path);
    llama_backend_init();
    LOGI("init: Backend initialized");
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_debanshu777_runner_LlamaRunner_nativeLoadModel(JNIEnv* env, jobject, jstring modelPath) {
    const char* path = env->GetStringUTFChars(modelPath, 0);
    LOGI("load: path=%s", path ? path : "(null)");
    if (!path || strlen(path) == 0) {
        LOGE("load: model_path is null or empty");
        env->ReleaseStringUTFChars(modelPath, path);
        return JNI_FALSE;
    }

    auto t0 = std::chrono::steady_clock::now();

    llama_log_set([](ggml_log_level level, const char* text, void* /*user_data*/) {
        int prio = (level >= GGML_LOG_LEVEL_ERROR) ? ANDROID_LOG_ERROR
            : (level >= GGML_LOG_LEVEL_WARN) ? ANDROID_LOG_WARN
            : ANDROID_LOG_INFO;
        __android_log_print(prio, LOG_TAG, "%s", text ? text : "");
    }, nullptr);

    llama_model_params model_params = llama_model_default_params();
    g_model = llama_model_load_from_file(path, model_params);
    env->ReleaseStringUTFChars(modelPath, path);

    auto t1 = std::chrono::steady_clock::now();
    auto load_ms = std::chrono::duration_cast<std::chrono::milliseconds>(t1 - t0).count();

    if (!g_model) {
        LOGE("load: Failed (took %lld ms)", (long long)load_ms);
        return JNI_FALSE;
    }
    LOGI("load: Model loaded in %lld ms", (long long)load_ms);

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 2048;
    ctx_params.n_threads = 4;
    ctx_params.n_batch = 512;
    LOGI("load: Creating context (n_ctx=2048, n_threads=4, n_batch=512)");

    g_context = llama_init_from_model(g_model, ctx_params);
    if (!g_context) {
        LOGE("load: Failed to create context");
        llama_model_free(g_model);
        g_model = nullptr;
        return JNI_FALSE;
    }
    LOGI("load: Context ready, n_ctx=%u", llama_n_ctx(g_context));

    g_batch = llama_batch_init(512, 0, 1);

    common_params_sampling sparams;
    sparams.temp = 0.3f;
    g_sampler = common_sampler_init(g_model, sparams);

    LOGI("load: Model ready (vocab_size=%d)", llama_vocab_n_tokens(llama_model_get_vocab(g_model)));
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_debanshu777_runner_LlamaRunner_nativeGenerateText(
    JNIEnv* env, jobject, jstring prompt, jint maxTokens) {

    LOGI("generate: entry max_tokens=%d", maxTokens);

    if (!g_model || !g_context || !g_sampler) {
        LOGE("generate: Model not loaded");
        return env->NewStringUTF("");
    }

    if (maxTokens <= 0) {
        LOGE("generate: max_tokens must be > 0");
        return env->NewStringUTF("");
    }

    const char* promptStr = env->GetStringUTFChars(prompt, 0);
    std::string promptStrCopy(promptStr ? promptStr : "");
    env->ReleaseStringUTFChars(prompt, promptStr);

    if (promptStrCopy.empty()) {
        LOGE("generate: Empty prompt");
        return env->NewStringUTF("");
    }

    llama_memory_clear(llama_get_memory(g_context), false);

    LOGI("generate: input prompt_len=%zu preview=\"%s\"", promptStrCopy.size(),
         truncate_for_log(promptStrCopy).c_str());

    const llama_vocab* vocab = llama_model_get_vocab(g_model);
    std::vector<llama_token> tokens = common_tokenize(vocab, promptStrCopy, true, true);
    LOGI("generate: tokenized n_tokens=%zu", tokens.size());

    if (tokens.empty()) {
        LOGE("generate: Tokenization produced no tokens");
        return env->NewStringUTF("");
    }

    auto t0 = std::chrono::steady_clock::now();
    std::string result;

    common_batch_clear(g_batch);
    for (size_t i = 0; i < tokens.size(); i++) {
        common_batch_add(g_batch, tokens[i], (llama_pos)i, {0}, i == tokens.size() - 1);
    }

    int decode_ret = llama_decode(g_context, g_batch);
    if (decode_ret != 0) {
        LOGE("generate: Prefill decode failed ret=%d", decode_ret);
        return env->NewStringUTF("");
    }
    LOGI("generate: Prefill ok");

    llama_synchronize(g_context);
    if (llama_get_logits_ith(g_context, -1) == nullptr) {
        LOGE("generate: n_outputs=0 after prefill");
        return env->NewStringUTF("");
    }

    int n_generated = 0;
    for (int i = 0; i < maxTokens; i++) {
        llama_synchronize(g_context);
        if (llama_get_logits_ith(g_context, -1) == nullptr) {
            LOGE("generate: n_outputs=0 at token %d, stopping", i);
            break;
        }
        llama_token token = common_sampler_sample(g_sampler, g_context, -1);

        if (llama_vocab_is_eog(vocab, token)) {
            LOGI("generate: EOG at token %d", i);
            break;
        }

        std::string piece = common_token_to_piece(vocab, token, true);
        result.append(piece);
        n_generated++;

        common_sampler_accept(g_sampler, token, true);

        common_batch_clear(g_batch);
        common_batch_add(g_batch, token, (llama_pos)(tokens.size() + i), {0}, true);

        decode_ret = llama_decode(g_context, g_batch);
        if (decode_ret != 0) {
            LOGE("generate: Decode failed at token %d ret=%d", i, decode_ret);
            break;
        }
    }

    auto t1 = std::chrono::steady_clock::now();
    auto elapsed_ms = std::chrono::duration_cast<std::chrono::milliseconds>(t1 - t0).count();
    LOGI("generate: done n_tokens=%d output_len=%zu elapsed_ms=%lld preview=\"%s\"",
         n_generated, result.size(), (long long)elapsed_ms, truncate_for_log(result).c_str());

    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_debanshu777_runner_LlamaRunner_nativeUnloadModel(JNIEnv*, jobject) {
    LOGI("unload: Releasing model and context");
    
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
    
    LOGI("unload: Model unloaded");
}

extern "C" JNIEXPORT void JNICALL
Java_com_debanshu777_runner_LlamaRunner_nativeShutdown(JNIEnv*, jobject) {
    LOGI("shutdown: Freeing backend");
    llama_backend_free();
}
