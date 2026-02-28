package com.debanshu777.runner

expect class LlamaRunner() {
    fun initialize(nativeLibDir: String)

    fun loadModel(modelPath: String): Boolean

    fun generateText(
        prompt: String,
        maxTokens: Int,
    ): String

    fun unloadModel()

    fun shutdown()
}
