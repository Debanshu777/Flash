package com.debanshu777.huggingfacemanager.usecase

import com.debanshu777.huggingfacemanager.api.SearchParams
import com.debanshu777.huggingfacemanager.api.error.DataError
import com.debanshu777.huggingfacemanager.api.error.Result
import com.debanshu777.huggingfacemanager.model.SearchResponse
import com.debanshu777.huggingfacemanager.repository.HuggingFaceRepository

class SearchModelsUseCase(
    private val repository: HuggingFaceRepository
) {
    suspend operator fun invoke(params: SearchParams): Result<SearchResponse, DataError.Network> =
        repository.searchModels(params)
}
