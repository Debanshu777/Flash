package com.debanshu777.runner

actual class LlamaRunner {
    
    init {
        System.loadLibrary("llama_runner")
    }
    
    actual fun initialize(nativeLibDir: String) {
        nativeInit(nativeLibDir)
    }
    
    actual fun loadModel(modelPath: String): Boolean {
        return nativeLoadModel(modelPath)
    }
    
    actual fun generateText(prompt: String, maxTokens: Int): String {
        if (prompt.isBlank() || maxTokens <= 0) return ""
        return nativeGenerateText(prompt, maxTokens)
    }
    
    actual fun unloadModel() {
        nativeUnloadModel()
    }
    
    actual fun shutdown() {
        nativeShutdown()
    }
    
    private external fun nativeInit(libDir: String)
    private external fun nativeLoadModel(modelPath: String): Boolean
    private external fun nativeGenerateText(prompt: String, maxTokens: Int): String
    private external fun nativeUnloadModel()
    private external fun nativeShutdown()
}
