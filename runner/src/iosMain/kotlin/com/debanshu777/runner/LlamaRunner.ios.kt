package com.debanshu777.runner

import com.debanshu777.runner.cpp.llama_runner_generate_text
import com.debanshu777.runner.cpp.llama_runner_init
import com.debanshu777.runner.cpp.llama_runner_load_model
import com.debanshu777.runner.cpp.llama_runner_shutdown
import com.debanshu777.runner.cpp.llama_runner_unload_model
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.free

actual class LlamaRunner {
    @OptIn(ExperimentalForeignApi::class)
    actual fun initialize(nativeLibDir: String) {
        llama_runner_init()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun loadModel(
        modelPath: String,
        nCtx: Int,
        nThreads: Int,
        nBatch: Int,
        nGpuLayers: Int,
        temperature: Float,
    ): Boolean {
        validateLoadModelArgs(modelPath)
        return llama_runner_load_model(modelPath, nCtx, nThreads, nBatch, nGpuLayers, temperature) != 0
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun generateText(
        prompt: String,
        maxTokens: Int,
        temperature: Float,
    ): String {
        validateGenerateArgs(prompt, maxTokens)
        val result = llama_runner_generate_text(prompt, maxTokens, temperature)
        return if (result != null) {
            try {
                result.toKString()
            } finally {
                free(result)
            }
        } else {
            ""
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun unloadModel() {
        llama_runner_unload_model()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun shutdown() {
        llama_runner_shutdown()
    }
}
