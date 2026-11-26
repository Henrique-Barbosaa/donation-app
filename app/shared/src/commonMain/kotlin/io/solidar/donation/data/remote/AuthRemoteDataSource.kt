package io.solidar.donation.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.solidar.donation.data.remote.dto.AuthResponse
import io.solidar.donation.data.remote.dto.LoginRequest
import io.solidar.donation.data.remote.dto.RegisterPfRequest
import io.solidar.donation.data.remote.dto.RegisterPjRequest
import io.solidar.donation.data.remote.dto.VerifyResponse

class AuthRemoteDataSource(
    private val httpClient: HttpClient = HttpClientFactory.create(),
) {
    private val baseUrl: String
        get() = ApiConfig.baseUrl

    suspend fun login(request: LoginRequest): AuthResponse =
        httpClient.post("$baseUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun registerPessoaFisica(request: RegisterPfRequest): AuthResponse =
        httpClient.post("$baseUrl/api/auth/registro/pessoa-fisica") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun registerInstituicao(request: RegisterPjRequest): AuthResponse =
        httpClient.post("$baseUrl/api/auth/registro/instituicao") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun verifySession(): VerifyResponse = httpClient.get("$baseUrl/api/auth/verify").body()
}
