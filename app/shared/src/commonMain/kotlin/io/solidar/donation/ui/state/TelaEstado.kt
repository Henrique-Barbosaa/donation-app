package io.solidar.donation.ui.state

import io.solidar.donation.domain.error.FalhaDominio

/**
 * Classe selada que representa os quatro estados obrigatórios de uma tela com carregamento de dados.
 * O tratamento na interface deve ser feito com `when` exaustivo (sem `else`).
 */
sealed interface TelaEstado<out T> {
    data object Carregando : TelaEstado<Nothing>

    data object Vazio : TelaEstado<Nothing>

    data class Sucesso<T>(
        val dados: T,
        val desatualizado: Boolean = false,
    ) : TelaEstado<T>

    data class Erro(
        val mensagem: String,
    ) : TelaEstado<Nothing>
}

/**
 * Traduz um [Throwable] (incluindo [FalhaDominio]) em uma mensagem amigável e distinta para o usuário.
 */
fun mapearMensagemDeErro(erro: Throwable): String =
    when (erro) {
        is FalhaDominio.SemConexao ->
            "Sem conexão com o servidor. Verifique sua internet ou se o backend está ativo."
        is FalhaDominio.RespostaInvalida ->
            "O servidor retornou uma resposta inválida. Tente novamente mais tarde."
        is FalhaDominio.DadosInvalidos ->
            erro.message
        else ->
            erro.message ?: "Ocorreu um erro inesperado. Tente novamente."
    }
