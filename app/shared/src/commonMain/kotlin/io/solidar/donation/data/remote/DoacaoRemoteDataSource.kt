package io.solidar.donation.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.solidar.donation.data.remote.dto.CriarDoacaoRequest
import io.solidar.donation.data.remote.dto.DoacaoResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DoacaoRemoteDataSource(
    private val httpClient: HttpClient = HttpClientFactory.create(),
    private val json: Json = HttpClientFactory.defaultJson,
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
                                            append(
                                                HttpHeaders.ContentDisposition,
                                                """filename="${imagemNome ?: "foto.jpg"}"""",
                                            )
                                        },
                                )
                            }
                        },
                    ),
                )
            }

        return response.body()
    }

    suspend fun listarMinhasDoacoes(): List<DoacaoResponse> {
        return httpClient.get("$baseUrl/api/doacoes/minhas").body()
    }

    suspend fun listarDoacoesRecebidas(): List<DoacaoResponse> {
        return httpClient.get("$baseUrl/api/doacoes/recebidas").body()
    }
}
