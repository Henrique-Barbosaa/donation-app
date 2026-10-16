package io.solidar.donation.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.solidar.donation.data.remote.dto.ApiErrorResponse
import io.solidar.donation.data.remote.dto.CriarDoacaoRequest
import io.solidar.donation.data.remote.dto.DoacaoResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DoacaoRemoteDataSource(
    private val httpClient: HttpClient = HttpClientFactory.create(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val baseUrl: String
        get() = ApiConfig.baseUrl

    suspend fun criarDoacao(
        request: CriarDoacaoRequest,
        imagemBytes: ByteArray? = null,
        imagemNome: String? = null,
    ): DoacaoResponse {
        val doacaoJson = json.encodeToString(request)
        val response =
            httpClient.post("$baseUrl/api/doacoes") {
                headers.remove(HttpHeaders.ContentType)
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "doacao",
                                value = doacaoJson,
                                headers =
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "application/json")
                                    },
                            )
                            if (imagemBytes != null && imagemBytes.isNotEmpty()) {
                                append(
                                    key = "imagem",
                                    value = imagemBytes,
                                    headers =
                                        Headers.build {
                                            append(HttpHeaders.ContentType, "image/jpeg")
                                            append(HttpHeaders.ContentDisposition, """filename="${imagemNome ?: "foto.jpg"}"""")
                                        },
                                )
                            }
                        },
                    ),
                )
            }

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw extractError(response.status.value, response.bodyAsText())
    }

    private fun extractError(
        statusCode: Int,
        responseBody: String,
    ): ApiException {
        return try {
            val errorResponse = json.decodeFromString<ApiErrorResponse>(responseBody)
            ApiException(
                statusCode = statusCode,
                message = errorResponse.mensagem ?: errorResponse.erro ?: "Erro na requisição (HTTP $statusCode)",
            )
        } catch (_: Exception) {
            ApiException(
                statusCode = statusCode,
                message = if (responseBody.isNotBlank()) responseBody else "Erro desconhecido (HTTP $statusCode)",
            )
        }
    }
}
