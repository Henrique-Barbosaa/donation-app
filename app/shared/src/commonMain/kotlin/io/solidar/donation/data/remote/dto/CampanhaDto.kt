package io.solidar.donation.data.remote.dto

import io.solidar.donation.domain.model.NovaCampanhaInput
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.CampaignItem
import io.solidar.donation.model.CampaignLocalColeta
import kotlinx.serialization.Serializable

@Serializable
data class LocalDoacaoRequestDto(
    val nome: String,
    val logradouro: String? = null,
    val bairro: String? = null,
    val numero: String? = null,
    val cep: String? = null,
)

@Serializable
data class CriarCampanhaRequest(
    val titulo: String,
    val descricao: String? = null,
    val itensFoco: String? = null,
    val metaVoluntarios: Int? = null,
    val metaEmCentavos: Long? = null,
    val locais: List<LocalDoacaoRequestDto>? = null,
)

fun NovaCampanhaInput.toCriarCampanhaRequest(): CriarCampanhaRequest {
    val locaisValidos =
        locais
            .filter { it.nome.isNotBlank() }
            .map { local ->
                LocalDoacaoRequestDto(
                    nome = local.nome.trim(),
                    logradouro = local.logradouro.trim().ifBlank { null },
                    bairro = local.bairro.trim().ifBlank { null },
                    numero = local.numero.trim().ifBlank { null },
                    cep = local.cep.filter { it.isDigit() }.ifBlank { null },
                )
            }
    return CriarCampanhaRequest(
        titulo = titulo.trim(),
        descricao = descricao.trim().ifBlank { null },
        itensFoco = itensFocoFormatado.ifBlank { null },
        metaVoluntarios = metaVoluntarios.takeIf { it > 0 },
        metaEmCentavos = metaEmCentavos.takeIf { it > 0L },
        locais = locaisValidos.takeIf { it.isNotEmpty() },
    )
}

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

private val ITEM_COM_QUANTIDADE_REGEX =
    Regex("""^(.+?)\s*\((\d+)\s*(?:un\.?|unidades?)?\)\s*$""", RegexOption.IGNORE_CASE)

/**
 * Conversão explícita de [CampanhaResponseDto] (camada `data`) para [Campaign] (camada `domain`).
 */
fun CampanhaResponseDto.toDomain(): Campaign {
    val idStr = (id ?: 0).toString()
    val tituloFinal = titulo?.takeIf { it.isNotBlank() } ?: "Campanha Solidária #$idStr"
    val organizadorFinal = nomeInstituicao?.takeIf { it.isNotBlank() } ?: "Instituição Parceira"
    val metaCentavos = (metaEmCentavos ?: 0L).coerceAtLeast(0L)
    val arrecadadoCentavos = (arrecadadoEmCentavos ?: 0L).coerceAtLeast(0L)

    val itens =
        itensFoco
            ?.split(",", ";")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.mapIndexed { index, itemBruto ->
                val match = ITEM_COM_QUANTIDADE_REGEX.matchEntire(itemBruto)
                val nomeLimpo = match?.groupValues?.get(1)?.trim()?.takeIf { it.isNotEmpty() } ?: itemBruto
                val quantidade = match?.groupValues?.get(2)?.toIntOrNull()?.coerceAtLeast(1)
                val quantidadeDescricao =
                    when (quantidade) {
                        null -> "Doação solicitada"
                        1 -> "1 unidade"
                        else -> "$quantidade unidades"
                    }
                CampaignItem(
                    id = "${idStr}_item_$index",
                    nome = nomeLimpo,
                    quantidadeDescricao = quantidadeDescricao,
                    iconeRef = "check",
                    quantidadeAlvo = quantidade,
                )
            }.orEmpty()

    val locaisMapeados =
        locais
            ?.mapNotNull { local ->
                val nomeLocal = local.nome?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                val endereco =
                    listOfNotNull(
                        local.logradouro?.trim()?.takeIf { it.isNotEmpty() },
                        local.numero?.trim()?.takeIf { it.isNotEmpty() }?.let { "nº $it" },
                        local.bairro?.trim()?.takeIf { it.isNotEmpty() },
                        local.cep?.trim()?.takeIf { it.isNotEmpty() }?.let { "CEP $it" },
                    ).joinToString(" • ")
                CampaignLocalColeta(nome = nomeLocal, enderecoResumo = endereco)
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
        locaisColeta = locaisMapeados,
        instituicaoId = instituicaoId,
        status = status?.takeIf { it.isNotBlank() }?.uppercase() ?: "ATIVA",
    )
}
