package io.solidar.donation.domain.model

/**
 * Envolve dados retornados pelo repositório indicando se vieram da rede em tempo real
 * ([desatualizado] = false) ou da cópia persistida localmente no aparelho ([desatualizado] = true).
 */
data class ResultadoCacheado<out T>(
    val dados: T,
    val desatualizado: Boolean = false,
)

/**
 * Dados de entrada do domínio para criação de uma nova doação, sem acoplamento com DTOs de rede.
 */
data class NovaDoacaoInput(
    val nome: String,
    val quantidade: Int,
    val categoria: String,
    val eNovo: Boolean,
    val descricao: String = "",
    val motivo: String = "",
)
