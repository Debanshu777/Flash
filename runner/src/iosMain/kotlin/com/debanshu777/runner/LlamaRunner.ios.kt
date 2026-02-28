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
    actual fun loadModel(modelPath: String): Boolean = llama_runner_load_model(modelPath) != 0

    @OptIn(ExperimentalForeignApi::class)
    actual fun generateText(
        prompt: String,
        maxTokens: Int,
    ): String {
        if (prompt.isBlank() || maxTokens <= 0) return ""
        val result = llama_runner_generate_text(prompt, maxTokens)
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
