package com.debanshu777.runner

actual class LlamaRunner {
    
    init {
        try {
            System.loadLibrary("llama_runner")
        } catch (e: UnsatisfiedLinkError) {
            System.err.println("Failed to load llama_runner library: ${e.message}")
            System.err.println("Make sure the native library is built and in java.library.path")
        }
    }
    
    actual fun initialize(nativeLibDir: String) {
        nativeInit(nativeLibDir)
    }
    
    actual fun loadModel(modelPath: String): Boolean {
        return nativeLoadModel(modelPath)
    }
    
    actual fun generateText(prompt: String, maxTokens: Int): String {
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

actual fun getPlatformInfo(): String = "${System.getProperty("os.name")} ${System.getProperty("os.arch")}"
