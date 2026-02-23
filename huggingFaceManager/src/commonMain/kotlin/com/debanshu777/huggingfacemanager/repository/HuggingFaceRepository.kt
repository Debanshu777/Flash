package com.debanshu777.huggingfacemanager.repository

import com.debanshu777.huggingfacemanager.api.RemoteHuggingFaceApiService
import com.debanshu777.huggingfacemanager.api.SearchParams
import com.debanshu777.huggingfacemanager.api.error.DataError
import com.debanshu777.huggingfacemanager.api.error.Result
import com.debanshu777.huggingfacemanager.model.ModelDetailResponse
import com.debanshu777.huggingfacemanager.model.SearchResponse

class HuggingFaceRepository(
    private val api: RemoteHuggingFaceApiService
) {
    suspend fun searchModels(params: SearchParams): Result<SearchResponse, DataError.Network> =
        api.searchModels(params)

    suspend fun getModelDetail(modelId: String): Result<ModelDetailResponse, DataError.Network> =
        api.getModelDetail(modelId)
}
