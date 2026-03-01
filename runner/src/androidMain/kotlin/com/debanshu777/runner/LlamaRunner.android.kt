package com.debanshu777.runner

actual class LlamaRunner {

    init {
        System.loadLibrary("llama_runner")
    }

    actual fun initialize(nativeLibDir: String) {
        nativeInit(nativeLibDir)
    }

    actual fun loadModel(
        modelPath: String,
        nCtx: Int,
        nThreads: Int,
        nBatch: Int,
        nGpuLayers: Int,
        temperature: Float,
    ): Boolean {
        validateLoadModelArgs(modelPath)
        return nativeLoadModel(modelPath, nCtx, nThreads, nBatch, nGpuLayers, temperature)
    }

    actual fun generateText(prompt: String, maxTokens: Int, temperature: Float): String {
        validateGenerateArgs(prompt, maxTokens)
        return nativeGenerateText(prompt, maxTokens, temperature)
    }

    actual fun unloadModel() {
        nativeUnloadModel()
    }

    actual fun shutdown() {
        nativeShutdown()
    }

    private external fun nativeInit(libDir: String)
    private external fun nativeLoadModel(
        modelPath: String,
        nCtx: Int,
        nThreads: Int,
        nBatch: Int,
        nGpuLayers: Int,
        temperature: Float,
    ): Boolean

    private external fun nativeGenerateText(
        prompt: String,
        maxTokens: Int,
        temperature: Float,
    ): String

    private external fun nativeUnloadModel()
    private external fun nativeShutdown()
}
