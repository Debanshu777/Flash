package com.debanshu777.huggingfacemanager.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    @SerialName("models")
    val models: List<Model?>?,
    @SerialName("numItemsPerPage")
    val numItemsPerPage: Int?,
    @SerialName("numTotalItems")
    val numTotalItems: Int?,
    @SerialName("pageIndex")
    val pageIndex: Int?
) {
    @Serializable
    data class Model(
        @SerialName("author")
        val author: String?,
        @SerialName("authorData")
        val authorData: AuthorData?,
        @SerialName("availableInferenceProviders")
        val availableInferenceProviders: List<AvailableInferenceProvider?>?,
        @SerialName("downloads")
        val downloads: Int?,
        @SerialName("gated")
        val gated: Boolean?,
        @SerialName("id")
        val id: String?,
        @SerialName("isLikedByUser")
        val isLikedByUser: Boolean?,
        @SerialName("lastModified")
        val lastModified: String?,
        @SerialName("likes")
        val likes: Int?,
        @SerialName("numParameters")
        val numParameters: Long?,
        @SerialName("pipeline_tag")
        val pipelineTag: String?,
        @SerialName("private")
        val `private`: Boolean?,
        @SerialName("repoType")
        val repoType: String?
    ) {
        @Serializable
        data class AuthorData(
            @SerialName("avatarUrl")
            val avatarUrl: String?,
            @SerialName("followerCount")
            val followerCount: Int?,
            @SerialName("fullname")
            val fullname: String?,
            @SerialName("_id")
            val id: String?,
            @SerialName("isHf")
            val isHf: Boolean?,
            @SerialName("isHfAdmin")
            val isHfAdmin: Boolean?,
            @SerialName("isMod")
            val isMod: Boolean?,
            @SerialName("isPro")
            val isPro: Boolean?,
            @SerialName("isUserFollowing")
            val isUserFollowing: Boolean?,
            @SerialName("name")
            val name: String?,
            @SerialName("plan")
            val plan: String?,
            @SerialName("type")
            val type: String?
        )

        @Serializable
        data class AvailableInferenceProvider(
            @SerialName("isCheapestPricingOutput")
            val isCheapestPricingOutput: Boolean?,
            @SerialName("isFastestThroughput")
            val isFastestThroughput: Boolean?,
            @SerialName("isModelAuthor")
            val isModelAuthor: Boolean?,
            @SerialName("modelStatus")
            val modelStatus: String?,
            @SerialName("provider")
            val provider: String?,
            @SerialName("providerId")
            val providerId: String?,
            @SerialName("providerStatus")
            val providerStatus: String?,
            @SerialName("task")
            val task: String?
        )
    }
}