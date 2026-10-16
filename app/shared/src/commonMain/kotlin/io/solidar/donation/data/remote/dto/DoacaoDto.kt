package io.solidar.donation.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItemRequest(
    val nome: String,
    val quantidade: Int,
    val descricao: String? = null,
    val motivo: String? = null,
    val eNovo: String? = null,
    val categoria: String? = null,
)

@Serializable
data class CriarDoacaoRequest(
    val itens: List<ItemRequest>,
)

@Serializable
data class ItemResponse(
    val id: Int? = null,
    val nome: String,
    val quantidade: Int,
    val descricao: String? = null,
    val motivo: String? = null,
    val eNovo: String? = null,
    val categoria: String? = null,
)

@Serializable
data class DoacaoResponse(
    val id: Int? = null,
    val doadorId: Int? = null,
    val nomeDoador: String? = null,
    val donatarioId: Int? = null,
    val nomeDonatario: String? = null,
    val itens: List<ItemResponse>? = null,
    val urlImagem: String? = null,
)
