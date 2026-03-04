package com.debanshu777.huggingfacemanager.model

enum class PipelineTag(val apiValue: String) {
    TEXT_GENERATION("text-generation");

    companion object {
        fun fromString(tag: String?): PipelineTag? =
            entries.firstOrNull { it.apiValue == tag }

        fun isSupported(tag: String?): Boolean =
            fromString(tag) == TEXT_GENERATION
    }
}
