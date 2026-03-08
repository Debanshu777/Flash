package com.debanshu777.caraml.features.chat.data

data class InferenceMetrics(
    val tpotMs: Double,
    val tokenCount: Int,
    val generationTimeMs: Long,
) {
    val tokensPerSecond: Double
        get() = if (tpotMs > 0.0) 1000.0 / tpotMs else 0.0
}
