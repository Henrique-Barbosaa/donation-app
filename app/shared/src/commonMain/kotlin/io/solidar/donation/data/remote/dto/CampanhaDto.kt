package io.solidar.donation.data.remote.dto

import io.solidar.donation.model.Campaign
import io.solidar.donation.model.CampaignItem
import kotlinx.serialization.Serializable

@Serializable
data class LocalDoacaoResponseDto(
    val id: Int? = null,
    val nome: String? = null,
    val logradouro: String? = null,
    val bairro: String? = null,
    val numero: String? = null,
    val cep: String? = null,
)

@Serializable
data class CampanhaResponseDto(
    val id: Int? = null,
    val instituicaoId: Int? = null,
    val nomeInstituicao: String? = null,
    val titulo: String? = null,
    val descricao: String? = null,
    val urlImagemCapa: String? = null,
    val dataInicio: String? = null,
    val dataFim: String? = null,
    val status: String? = null,
    val itensFoco: String? = null,
    val metaVoluntarios: Int? = null,
    val locais: List<LocalDoacaoResponseDto>? = null,
    val metaEmCentavos: Long? = null,
    val arrecadadoEmCentavos: Long? = null,
)

@Serializable
data class PaginaResponseDto<T>(
    val content: List<T> = emptyList(),
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val currentPage: Int = 0,
)

/**
 * Conversão explícita de [CampanhaResponseDto] (camada `data`) para [Campaign] (camada `domain`).
 */
fun CampanhaResponseDto.toDomain(): Campaign {
    val idStr = (id ?: 0).toString()
    val tituloFinal = titulo?.takeIf { it.isNotBlank() } ?: "Campanha Solidária #$idStr"
    val organizadorFinal = nomeInstituicao?.takeIf { it.isNotBlank() } ?: "Instituição Parceira"
    val metaCentavos =
        metaEmCentavos
            ?: ((metaVoluntarios ?: 50).toLong().coerceAtLeast(1L) * 10_000L)
    val arrecadadoCentavos =
        arrecadadoEmCentavos
            ?: (metaCentavos * 60L / 100L)

    val itens =
        itensFoco
            ?.split(",", ";")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.mapIndexed { index, itemNome ->
                CampaignItem(
                    id = "${idStr}_item_$index",
                    nome = itemNome,
                    quantidadeDescricao = "Doação solicitada",
                    iconeRef = "check",
                )
            }.orEmpty()

    return Campaign(
        id = idStr,
        title = tituloFinal,
        organizer = organizadorFinal,
        goalInCents = metaCentavos,
        raisedInCents = arrecadadoCentavos,
        descricao = descricao.orEmpty(),
        imageUrl = urlImagemCapa,
        itensNecessarios = itens,
    )
}
