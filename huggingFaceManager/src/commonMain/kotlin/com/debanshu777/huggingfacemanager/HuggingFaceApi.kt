package com.debanshu777.huggingfacemanager

import com.debanshu777.huggingfacemanager.api.RemoteHuggingFaceApiService
import com.debanshu777.huggingfacemanager.repository.HuggingFaceRepository
import com.debanshu777.huggingfacemanager.usecase.GetModelDetailUseCase
import com.debanshu777.huggingfacemanager.usecase.SearchModelsUseCase
import kotlinx.serialization.json.Json

interface HuggingFaceApi {
    val searchModels: SearchModelsUseCase
    val getModelDetail: GetModelDetailUseCase
}

object HuggingFaceConstants {
    const val DEFAULT_BASE_URL = "https://huggingface.co"
}

fun createHuggingFaceApi(baseUrl: String = HuggingFaceConstants.DEFAULT_BASE_URL): HuggingFaceApi {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    val httpClient = createPlatformHttpClient { }
    val api = RemoteHuggingFaceApiService(httpClient, json, baseUrl)
    val repository = HuggingFaceRepository(api)
    return DefaultHuggingFaceApi(
        searchModels = SearchModelsUseCase(repository),
        getModelDetail = GetModelDetailUseCase(repository)
    )
}

private class DefaultHuggingFaceApi(
    override val searchModels: SearchModelsUseCase,
    override val getModelDetail: GetModelDetailUseCase
) : HuggingFaceApi
