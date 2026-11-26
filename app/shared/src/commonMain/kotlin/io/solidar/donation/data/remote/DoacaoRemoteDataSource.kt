package io.solidar.donation.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.solidar.donation.data.remote.dto.CriarDoacaoRequest
import io.solidar.donation.data.remote.dto.DoacaoResponse
import io.solidar.donation.data.remote.dto.EfetivarDoacaoRequest
import io.solidar.donation.data.remote.dto.PaginaResponseDto
import io.solidar.donation.data.remote.dto.RegistrarInteresseRequest
import io.solidar.donation.data.remote.dto.UsuarioInteressadoDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

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

    suspend fun listarDoacoesDisponiveis(
        page: Int = 0,
        size: Int = 20,
        categoria: String? = null,
        busca: String? = null,
    ): List<DoacaoResponse> {
        val response =
            httpClient.get("$baseUrl/api/doacoes/disponiveis") {
                parameter("page", page)
                parameter("size", size)
                categoria?.takeIf { it.isNotBlank() }?.let { parameter("categoria", it.uppercase()) }
                busca?.takeIf { it.isNotBlank() }?.let { parameter("busca", it.trim()) }
            }
        val rawBody = response.bodyAsText()
        val element = json.parseToJsonElement(rawBody)
        return if (element is JsonArray) {
            json.decodeFromString<List<DoacaoResponse>>(rawBody)
        } else {
            json.decodeFromString<PaginaResponseDto<DoacaoResponse>>(rawBody).content
        }
    }

    suspend fun registrarInteresse(
        doacaoId: String,
        mensagem: String? = null,
    ) {
        val texto = mensagem?.trim()?.takeIf { it.isNotEmpty() }
        if (texto != null) {
            httpClient.post("$baseUrl/api/doacoes/$doacaoId/interesse") {
                contentType(ContentType.Application.Json)
                setBody(RegistrarInteresseRequest(mensagem = texto))
            }
        } else {
            httpClient.post("$baseUrl/api/doacoes/$doacaoId/interesse")
        }
    }

    suspend fun removerInteresse(doacaoId: String) {
        httpClient.delete("$baseUrl/api/doacoes/$doacaoId/interesse")
    }

    suspend fun listarInteressados(doacaoId: String): List<UsuarioInteressadoDto> {
        val response = httpClient.get("$baseUrl/api/doacoes/$doacaoId/interessados")
        val rawBody = response.bodyAsText().trim()
        if (rawBody.isEmpty()) return emptyList()
        val element = json.parseToJsonElement(rawBody)
        return if (element is JsonArray) {
            json.decodeFromString<List<UsuarioInteressadoDto>>(rawBody)
        } else {
            json.decodeFromString<PaginaResponseDto<UsuarioInteressadoDto>>(rawBody).content
        }
    }

    suspend fun efetivarDoacao(
        doacaoId: String,
        request: EfetivarDoacaoRequest,
    ): DoacaoResponse {
        return httpClient.put("$baseUrl/api/doacoes/$doacaoId/efetivar") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun cancelarDoacao(doacaoId: String) {
        httpClient.delete("$baseUrl/api/doacoes/$doacaoId")
    }
}
