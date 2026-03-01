package com.debanshu777.runner

internal fun validateLoadModelArgs(modelPath: String) {
    require(modelPath.isNotBlank()) { "modelPath must not be blank" }
}

internal fun validateGenerateArgs(prompt: String, maxTokens: Int) {
    require(prompt.isNotBlank()) { "prompt must not be blank" }
    require(maxTokens > 0) { "maxTokens must be > 0" }
}
