package com.debanshu777.runner

expect class LlamaRunner() {
    fun initialize(nativeLibDir: String)

    fun loadModel(
        modelPath: String,
        nCtx: Int = 2048,
        nThreads: Int = 4,
        nBatch: Int = 512,
        nGpuLayers: Int = 0,
        temperature: Float = 0.3f,
    ): Boolean

    fun generateText(
        prompt: String,
        maxTokens: Int = 256,
        temperature: Float = -1.0f,
    ): String

    fun unloadModel()

    fun shutdown()
}
