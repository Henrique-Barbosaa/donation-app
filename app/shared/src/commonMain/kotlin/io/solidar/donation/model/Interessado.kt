package io.solidar.donation.model

/**
 * Usuário que demonstrou interesse em uma doação (usado na efetivação da doação — issue #10).
 */
data class Interessado(
    val id: Int,
    val nome: String,
    val email: String,
    val enderecoResumo: String = "",
    val mensagem: String = "",
)
