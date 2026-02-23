package com.debanshu777.huggingfacemanager.api

import com.debanshu777.huggingfacemanager.api.error.DataError
import com.debanshu777.huggingfacemanager.api.error.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class ClientWrapper(
    @PublishedApi internal val networkClient: HttpClient,
    @PublishedApi internal val json: Json,
) {
    suspend inline fun <reified T> networkGetUsecase(
        endpoint: String,
        queries: Map<String, String>? = null,
    ): Result<T, DataError.Network> {
        val response = try {
            networkClient.get(endpoint) {
                queries?.forEach { (key, value) ->
                    parameter(key, value)
                }
            }
        } catch (_: UnresolvedAddressException) {
            return Result.Error(DataError.Network.NoInternet)
        } catch (_: SerializationException) {
            return Result.Error(DataError.Network.Serialization)
        } catch (_: Exception) {
            return Result.Error(DataError.Network.Unknown)
        }

        return when (response.status.value) {
            in 200..299 -> {
                try {
                    val bodyText = response.bodyAsText()
                    val data = json.decodeFromString<T>(bodyText)
                    Result.Success(data)
                } catch (_: SerializationException) {
                    Result.Error(DataError.Network.Serialization)
                }
            }
            401 -> Result.Error(DataError.Network.Unauthorized)
            408 -> Result.Error(DataError.Network.RequestTimeout)
            409 -> Result.Error(DataError.Network.Conflict)
            413 -> Result.Error(DataError.Network.PayloadTooLarge)
            in 500..599 -> Result.Error(DataError.Network.ServerError)
            else -> Result.Error(DataError.Network.Unknown)
        }
    }
}
