package io.solidar.donation.data.remote

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.ContentConvertException
import io.ktor.serialization.JsonConvertException
import io.solidar.donation.data.remote.dto.ApiErrorResponse
import io.solidar.donation.domain.error.FalhaDominio
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Traduz exceções de transporte e serialização do Ktor Client nas falhas semânticas do domínio:
 * - [FalhaDominio.SemConexao] quando o backend está inacessível, parado ou ocorre timeout.
 * - [FalhaDominio.RespostaInvalida] quando o servidor responde com HTTP 5xx ou JSON fora do formato.
 * - [FalhaDominio.DadosInvalidos] quando o servidor responde com HTTP 4xx de regra de negócio/autenticação.
 */
object NetworkErrorMapper {
    suspend fun mapear(
        throwable: Throwable,
        json: Json = HttpClientFactory.defaultJson,
    ): FalhaDominio {
        if (throwable is FalhaDominio) return throwable

        if (throwable is ServerResponseException) {
            val statusCode = throwable.response.status.value
            return FalhaDominio.RespostaInvalida(
                statusCode = statusCode,
                message = "Erro interno no servidor (HTTP $statusCode). Tente novamente mais tarde.",
                cause = throwable,
            )
        }

        if (throwable is ClientRequestException) {
            val statusCode = throwable.response.status.value
            val bodyText = runCatching { throwable.response.bodyAsText() }.getOrDefault("")
            val parsedMessage =
                runCatching {
                    val errorDto = json.decodeFromString<ApiErrorResponse>(bodyText)
                    errorDto.mensagem ?: errorDto.erro
                }.getOrNull()

            if (statusCode == 400 || statusCode == 401 || statusCode == 403 || statusCode == 409) {
                val msg =
                    parsedMessage ?: when (statusCode) {
                        401, 403 -> "Credenciais inválidas ou sessão expirada."
                        409 -> "E-mail ou documento já cadastrado no sistema."
                        else -> "Dados informados são inválidos. Verifique os campos."
                    }
                return FalhaDominio.DadosInvalidos(message = msg, cause = throwable)
            }

            return FalhaDominio.RespostaInvalida(
                statusCode = statusCode,
                message = parsedMessage ?: "Resposta inesperada do servidor (HTTP $statusCode).",
                cause = throwable,
            )
        }

        if (throwable is ResponseException) {
            val statusCode = throwable.response.status.value
            return FalhaDominio.RespostaInvalida(
                statusCode = statusCode,
                message = "Resposta inválida do servidor (HTTP $statusCode).",
                cause = throwable,
            )
        }

        if (
            throwable is SerializationException ||
            throwable is JsonConvertException ||
            throwable is ContentConvertException ||
            throwable.cause is SerializationException
        ) {
            return FalhaDominio.RespostaInvalida(
                message = "Os dados recebidos do servidor estão em um formato inválido.",
                cause = throwable,
            )
        }

        return FalhaDominio.SemConexao(
            message = "Não foi possível conectar ao servidor. Verifique se o backend está ativo e sua conexão de rede.",
            cause = throwable,
        )
    }
}
