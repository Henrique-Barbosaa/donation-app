package io.solidar.donation.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.solidar.donation.data.remote.dto.CampanhaResponseDto
import io.solidar.donation.data.remote.dto.CriarCampanhaRequest
import io.solidar.donation.data.remote.dto.PaginaResponseDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

class CampanhaRemoteDataSource(
    private val httpClient: HttpClient = HttpClientFactory.create(),
    private val json: Json = HttpClientFactory.defaultJson,
) {
    private val baseUrl: String
        get() = ApiConfig.baseUrl

    suspend fun listarCampanhas(
        page: Int = 0,
        size: Int = 20,
    ): List<CampanhaResponseDto> {
        val response = httpClient.get("$baseUrl/api/campanhas?page=$page&size=$size")
        val rawBody = response.bodyAsText()
        val element = json.parseToJsonElement(rawBody)
        return if (element is JsonArray) {
            json.decodeFromString<List<CampanhaResponseDto>>(rawBody)
        } else {
            json.decodeFromString<PaginaResponseDto<CampanhaResponseDto>>(rawBody).content
        }
    }

    suspend fun obterCampanhaPorId(id: String): CampanhaResponseDto {
        val response = httpClient.get("$baseUrl/api/campanhas/$id")
        return json.decodeFromString<CampanhaResponseDto>(response.bodyAsText())
    }

    suspend fun listarMinhasCampanhas(): List<CampanhaResponseDto> {
        val response = httpClient.get("$baseUrl/api/campanhas/minhas")
        return json.decodeFromString<List<CampanhaResponseDto>>(response.bodyAsText())
    }

    suspend fun finalizarCampanha(id: String): CampanhaResponseDto {
        val response = httpClient.put("$baseUrl/api/campanhas/$id/finalizar")
        return json.decodeFromString<CampanhaResponseDto>(response.bodyAsText())
    }

    suspend fun cancelarCampanha(id: String) {
        httpClient.put("$baseUrl/api/campanhas/$id/cancelar")
    }

    suspend fun criarCampanha(request: CriarCampanhaRequest): CampanhaResponseDto {
        val campanhaJson = json.encodeToString(request)
        val response =
            httpClient.post("$baseUrl/api/campanhas") {
                headers.remove(HttpHeaders.ContentType)
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "campanha",
                                value = campanhaJson,
                                headers =
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "application/json")
                                    },
                            )
                        },
                    ),
                )
            }
        return response.body()
    }
}
