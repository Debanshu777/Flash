package com.debanshu777.huggingfacemanager.api

import com.debanshu777.huggingfacemanager.model.ModelSort
import com.debanshu777.huggingfacemanager.model.ParameterRange

/**
 * Validated search parameters for Hugging Face models API.
 *
 * @param minParams Minimum parameter range (default: ZERO)
 * @param maxParams Maximum parameter range (default: SIX_B). Must be >= minParams.
 * @param library Library filters (default: gguf, onnx)
 * @param sort Sort order (default: TRENDING)
 * @param page Page index, 0-based (default: 0)
 * @param withCount Include total count in response (default: true)
 */
data class SearchParams(
    val minParams: ParameterRange = ParameterRange.ZERO,
    val maxParams: ParameterRange = ParameterRange.SIX_B,
    val library: List<String> = DEFAULT_LIBRARY,
    val sort: ModelSort = ModelSort.TRENDING,
    val page: Int = 0,
    val withCount: Boolean = true
) {
    init {
        require(minParams.ordinal <= maxParams.ordinal) {
            "minParams cannot be greater than maxParams"
        }
        require(page >= 0) {
            "page must be non-negative"
        }
    }

    companion object {
        private val DEFAULT_LIBRARY = listOf("gguf", "onnx")
    }
}
