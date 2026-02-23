package com.debanshu777.huggingfacemanager.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModelDetailResponse(
    @SerialName("author")
    val author: String?,
    @SerialName("cardData")
    val cardData: CardData?,
    @SerialName("config")
    val config: Config?,
    @SerialName("createdAt")
    val createdAt: String?,
    @SerialName("disabled")
    val disabled: Boolean?,
    @SerialName("downloads")
    val downloads: Int?,
    @SerialName("gated")
    val gated: Boolean?,
    @SerialName("gguf")
    val gguf: Gguf?,
    @SerialName("_id")
    val _id: String?,
    @SerialName("id")
    val id: String?,
    @SerialName("lastModified")
    val lastModified: String?,
    @SerialName("library_name")
    val libraryName: String?,
    @SerialName("likes")
    val likes: Int?,
    @SerialName("modelId")
    val modelId: String?,
    @SerialName("pipeline_tag")
    val pipelineTag: String?,
    @SerialName("private")
    val `private`: Boolean?,
    @SerialName("safetensors")
    val safetensors: Safetensors?,
    @SerialName("sha")
    val sha: String?,
    @SerialName("siblings")
    val siblings: List<Sibling?>?,
    @SerialName("tags")
    val tags: List<String?>?,
    @SerialName("usedStorage")
    val usedStorage: Long?
) {
    @Serializable
    data class CardData(
        @SerialName("base_model")
        val baseModel: String?,
        @SerialName("base_model_relation")
        val baseModelRelation: String?,
        @SerialName("inference")
        val inference: Boolean?,
        @SerialName("language")
        val language: List<String?>?,
        @SerialName("library_name")
        val libraryName: String?,
        @SerialName("license")
        val license: String?,
        @SerialName("pipeline_tag")
        val pipelineTag: String?,
        @SerialName("tags")
        val tags: List<String?>?
    )

    @Serializable
    data class Config(
        @SerialName("architectures")
        val architectures: List<String?>?,
        @SerialName("auto_map")
        val autoMap: AutoMap?,
        @SerialName("model_type")
        val modelType: String?,
        @SerialName("tokenizer_config")
        val tokenizerConfig: TokenizerConfig?
    ) {
        @Serializable
        data class AutoMap(
            @SerialName("AutoConfig")
            val autoConfig: String?,
            @SerialName("AutoModel")
            val autoModel: String?,
            @SerialName("AutoModelForMaskedLM")
            val autoModelForMaskedLM: String?
        )

        @Serializable
        data class TokenizerConfig(
            @SerialName("bos_token")
            val bosToken: String?,
            @SerialName("eos_token")
            val eosToken: String?,
            @SerialName("mask_token")
            val maskToken: String?,
            @SerialName("pad_token")
            val padToken: String?
        )
    }

    @Serializable
    data class Gguf(
        @SerialName("architecture")
        val architecture: String?,
        @SerialName("bos_token")
        val bosToken: String?,
        @SerialName("causal")
        val causal: Boolean?,
        @SerialName("context_length")
        val contextLength: Int?,
        @SerialName("eos_token")
        val eosToken: String?,
        @SerialName("total")
        val total: Int?
    )

    @Serializable
    data class Safetensors(
        @SerialName("parameters")
        val parameters: Parameters?,
        @SerialName("total")
        val total: Int?
    ) {
        @Serializable
        data class Parameters(
            @SerialName("BF16")
            val bF16: Int?
        )
    }

    @Serializable
    data class Sibling(
        @SerialName("rfilename")
        val rfilename: String?
    )
}