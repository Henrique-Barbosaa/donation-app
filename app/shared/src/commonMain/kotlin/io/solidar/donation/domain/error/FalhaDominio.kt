package io.solidar.donation.domain.error

/**
 * Falhas semânticas do domínio, independentes de biblioteca de rede ou persistência.
 */
sealed class FalhaDominio(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause) {
    /** Falha de conectividade: servidor inacessível, timeout ou dispositivo sem rede. */
    class SemConexao(
        message: String = "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.",
        cause: Throwable? = null,
    ) : FalhaDominio(message, cause)

    /** Resposta inesperada do servidor: erro HTTP 5xx, status inesperado ou JSON malformado. */
    class RespostaInvalida(
        val statusCode: Int? = null,
        message: String = "O servidor retornou uma resposta inválida. Tente novamente mais tarde.",
        cause: Throwable? = null,
    ) : FalhaDominio(message, cause)

    /** Erro de validação ou regra de negócio retornado pela API (ex.: 400, 401, 409). */
    class DadosInvalidos(
        message: String,
        cause: Throwable? = null,
    ) : FalhaDominio(message, cause)
}
