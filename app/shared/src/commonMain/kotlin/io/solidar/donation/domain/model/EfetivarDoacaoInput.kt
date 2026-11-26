package io.solidar.donation.domain.model

/**
 * Entrada pura da camada de domínio para efetivar a entrega de uma doação a um interessado.
 */
data class EfetivarDoacaoInput(
    val donatarioId: Int,
    val nomeLocal: String,
    val logradouro: String = "",
    val bairro: String = "",
    val numero: String = "",
    val cep: String = "",
)
