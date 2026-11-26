package io.solidar.donation.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.solidar.donation.data.remote.dto.AlterarSenhaRequestDto
import io.solidar.donation.data.remote.dto.AtualizarPerfilRequestDto
import io.solidar.donation.data.remote.dto.InstituicaoResponseDto
import io.solidar.donation.data.remote.dto.PessoaFisicaResponseDto

class PerfilRemoteDataSource(
    private val httpClient: HttpClient = HttpClientFactory.create(),
) {
    private val baseUrl: String
        get() = ApiConfig.baseUrl

    suspend fun buscarPessoaFisica(id: Int): PessoaFisicaResponseDto {
        return httpClient.get("$baseUrl/api/usuarios/pessoa-fisica/$id").body()
    }

    suspend fun buscarInstituicao(id: Int): InstituicaoResponseDto {
        return httpClient.get("$baseUrl/api/usuarios/instituicao/$id").body()
    }

    suspend fun atualizarPerfil(request: AtualizarPerfilRequestDto) {
        httpClient.put("$baseUrl/api/usuarios/perfil") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun alterarSenha(request: AlterarSenhaRequestDto) {
        httpClient.put("$baseUrl/api/usuarios/senha") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
