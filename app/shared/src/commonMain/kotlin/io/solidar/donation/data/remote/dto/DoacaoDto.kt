package io.solidar.donation.data.remote.dto

import io.solidar.donation.domain.model.NovaDoacaoInput
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.model.DoacaoStatus
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
    val dataCriacao: String? = null,
)

/**
 * Converte a entrada pura do domínio [NovaDoacaoInput] para o DTO de rede [CriarDoacaoRequest].
 */
fun NovaDoacaoInput.toCriarDoacaoRequest(): CriarDoacaoRequest {
    val item =
        ItemRequest(
            nome = nome.trim(),
            quantidade = quantidade,
            descricao = descricao.trim().ifBlank { null },
            motivo = motivo.trim().ifBlank { null },
            eNovo = if (eNovo) "S" else "N",
            categoria = categoria.uppercase(),
        )
    return CriarDoacaoRequest(itens = listOf(item))
}

/**
 * Converte o DTO de rede [DoacaoResponse] para a entidade do domínio [Doacao].
 */
fun DoacaoResponse.toDomain(direction: DoacaoDirecao): Doacao {
    val primeiroItem = itens?.firstOrNull()
    val tituloItem =
        when {
            primeiroItem == null -> "Doação #${id ?: 0}"
            primeiroItem.quantidade > 1 -> "${primeiroItem.nome} (${primeiroItem.quantidade} un.)"
            else -> primeiroItem.nome
        }
    val contraparte =
        when (direction) {
            DoacaoDirecao.FEITA -> nomeDonatario?.takeIf { it.isNotBlank() } ?: "Aguardando interessado"
            DoacaoDirecao.RECEBIDA -> nomeDoador?.takeIf { it.isNotBlank() } ?: "Doador Solidário"
        }
    val statusDoacao =
        if (donatarioId != null && donatarioId > 0) {
            DoacaoStatus.CONCLUIDA
        } else {
            DoacaoStatus.PENDENTE
        }

    return Doacao(
        id = (id ?: 0).toString(),
        itemTitle = tituloItem,
        counterpartName = contraparte,
        direction = direction,
        status = statusDoacao,
        dateLabel = dataCriacao?.takeIf { it.isNotBlank() } ?: "Registrada recentemente",
    )
}
