package io.solidar.donation.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.solidar.donation.data.remote.dto.ApiErrorResponse
import io.solidar.donation.data.remote.dto.AuthResponse
import io.solidar.donation.data.remote.dto.LoginRequest
import io.solidar.donation.data.remote.dto.RegisterPfRequest
import io.solidar.donation.data.remote.dto.RegisterPjRequest
import io.solidar.donation.data.remote.dto.VerifyResponse
import kotlinx.serialization.json.Json

class ApiException(
    val statusCode: Int,
    override val message: String,
) : Exception(message)

class AuthRemoteDataSource(
    private val httpClient: HttpClient = HttpClientFactory.create(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val baseUrl: String
        get() = ApiConfig.baseUrl

    suspend fun login(request: LoginRequest): AuthResponse {
        val response =
            httpClient.post("$baseUrl/api/auth/login") {
                setBody(request)
            }

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw extractError(response.status.value, response.bodyAsText())
    }

    suspend fun registerPessoaFisica(request: RegisterPfRequest): AuthResponse {
        val response =
            httpClient.post("$baseUrl/api/auth/registro/pessoa-fisica") {
                setBody(request)
            }

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw extractError(response.status.value, response.bodyAsText())
    }

    suspend fun registerInstituicao(request: RegisterPjRequest): AuthResponse {
        val response =
            httpClient.post("$baseUrl/api/auth/registro/instituicao") {
                setBody(request)
            }

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw extractError(response.status.value, response.bodyAsText())
    }

    suspend fun verifySession(): VerifyResponse {
        val response = httpClient.get("$baseUrl/api/auth/verify")

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw extractError(response.status.value, response.bodyAsText())
    }

    private fun extractError(
        statusCode: Int,
        responseBody: String,
    ): ApiException {
        val parsedMessage =
            try {
                val errorDto = json.decodeFromString<ApiErrorResponse>(responseBody)
                errorDto.mensagem ?: errorDto.erro
            } catch (_: Exception) {
                null
            }

        val message =
            parsedMessage ?: when (statusCode) {
                401 -> "Credenciais inválidas ou sessão expirada."
                409 -> "E-mail ou documento já cadastrado no sistema."
                400 -> "Dados informados são inválidos. Verifique os campos."
                404 -> "Recurso não encontrado."
                500 -> "Erro interno do servidor. Tente novamente mais tarde."
                else -> "Ocorreu um erro ao comunicar com o servidor (HTTP $statusCode)."
            }

        return ApiException(statusCode, message)
    }
}
