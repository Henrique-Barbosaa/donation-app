package io.solidar.donation.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.solidar.donation.data.remote.dto.CampanhaResponseDto
import io.solidar.donation.data.remote.dto.PaginaResponseDto
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
}
