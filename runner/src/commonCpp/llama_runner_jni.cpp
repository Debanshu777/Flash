#include <jni.h>

#include <cstdio>
#include <string>

#ifdef __ANDROID__
#include <android/log.h>
#endif

#include "llama_runner_core.h"

namespace {

constexpr const char *LOG_TAG = "LlamaRunner";

void platform_log(LlamaLogLevel level, const char *msg) {
#ifdef __ANDROID__
    int prio = ANDROID_LOG_INFO;
    if (level == LLAMA_LOG_WARN) {
        prio = ANDROID_LOG_WARN;
    } else if (level == LLAMA_LOG_ERROR) {
        prio = ANDROID_LOG_ERROR;
    }
    __android_log_print(prio, LOG_TAG, "%s", msg ? msg : "");
#else
    const char *lvl = "INFO";
    if (level == LLAMA_LOG_WARN) {
        lvl = "WARN";
    } else if (level == LLAMA_LOG_ERROR) {
        lvl = "ERROR";
    }
    std::fprintf(stderr, "[%s] [%s] %s\n", LOG_TAG, lvl, msg ? msg : "");
#endif
}

} // namespace

extern "C" JNIEXPORT void JNICALL
Java_com_debanshu777_runner_LlamaRunner_nativeInit(JNIEnv *env, jobject, jstring libDir) {
    const char *path = env->GetStringUTFChars(libDir, 0);
    llama_runner_core_set_logger(platform_log);
    llama_runner_core_init(path);
    env->ReleaseStringUTFChars(libDir, path);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_debanshu777_runner_LlamaRunner_nativeLoadModel(
    JNIEnv *env,
    jobject,
    jstring modelPath,
    jint nCtx,
    jint nThreads,
    jint nBatch,
    jint nGpuLayers,
    jfloat temperature) {
    const char *path = env->GetStringUTFChars(modelPath, 0);

    LlamaRunnerConfig config;
    config.n_ctx = static_cast<int>(nCtx);
    config.n_threads = static_cast<int>(nThreads);
    config.n_batch = static_cast<int>(nBatch);
    config.n_gpu_layers = static_cast<int>(nGpuLayers);
    config.temperature = static_cast<float>(temperature);

    const bool ok = llama_runner_core_load_model(path, config);
    env->ReleaseStringUTFChars(modelPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_debanshu777_runner_LlamaRunner_nativeGenerateText(
    JNIEnv *env,
    jobject,
    jstring prompt,
    jint maxTokens,
    jfloat temperature) {
    const char *prompt_str = env->GetStringUTFChars(prompt, 0);
    const std::string result = llama_runner_core_generate(
        prompt_str,
        static_cast<int>(maxTokens),
        static_cast<float>(temperature));
    env->ReleaseStringUTFChars(prompt, prompt_str);
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_debanshu777_runner_LlamaRunner_nativeUnloadModel(JNIEnv *, jobject) {
    llama_runner_core_unload();
}

extern "C" JNIEXPORT void JNICALL
Java_com_debanshu777_runner_LlamaRunner_nativeShutdown(JNIEnv *, jobject) {
    llama_runner_core_shutdown();
}
