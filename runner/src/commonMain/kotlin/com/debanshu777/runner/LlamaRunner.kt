package com.debanshu777.runner

// Main API for model inference
expect class LlamaRunner() {
    fun initialize(nativeLibDir: String)
    fun loadModel(modelPath: String): Boolean
    fun generateText(prompt: String, maxTokens: Int): String
    fun unloadModel()
    fun shutdown()
}

// Utility for getting platform info
expect fun getPlatformInfo(): String
