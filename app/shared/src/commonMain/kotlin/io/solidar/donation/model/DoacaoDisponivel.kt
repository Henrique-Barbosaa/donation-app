package io.solidar.donation.model

/**
 * Item de doação disponível no feed público (aba Explorar).
 */
data class DoacaoDisponivel(
    val id: String,
    val itemTitle: String,
    val quantidade: Int,
    val categoria: String,
    val condicao: String,
    val descricao: String,
    val motivo: String,
    val nomeDoador: String,
    val urlImagem: String? = null,
    val doadorId: Int? = null,
)
