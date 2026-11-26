package io.solidar.donation.data.remote.dto

import io.solidar.donation.domain.model.EfetivarDoacaoInput
import io.solidar.donation.domain.model.NovaDoacaoInput
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.model.DoacaoDisponivel
import io.solidar.donation.model.DoacaoStatus
import io.solidar.donation.model.Interessado
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
    val donatarioId: Int? = null,
    val campanhaId: Int? = null,
    val valorEmCentavos: Long? = null,
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
    return CriarDoacaoRequest(
        itens = listOf(item),
        donatarioId = donatarioId,
        campanhaId = campanhaId,
        valorEmCentavos = valorEmCentavos,
    )
}

/**
 * Converte o DTO de rede [DoacaoResponse] para a entidade do domínio [Doacao].
 */
fun DoacaoResponse.toDomain(direction: DoacaoDirecao): Doacao {
    val listaItens = itens.orEmpty()
    val primeiroItem = listaItens.firstOrNull()
    val extras = (listaItens.size - 1).coerceAtLeast(0)
    val baseTitulo =
        when {
            primeiroItem == null -> "Doação #${id ?: 0}"
            primeiroItem.quantidade > 1 -> "${primeiroItem.nome} (${primeiroItem.quantidade} un.)"
            else -> primeiroItem.nome
        }
    val tituloItem = if (extras > 0) "$baseTitulo +$extras" else baseTitulo
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

/**
 * Converte [DoacaoResponse] em [DoacaoDisponivel] para o feed público da aba Explorar.
 */
fun DoacaoResponse.toDisponivelDomain(): DoacaoDisponivel {
    val primeiroItem = itens?.firstOrNull()
    val titulo = primeiroItem?.nome?.takeIf { it.isNotBlank() } ?: "Doação #${id ?: 0}"
    val categoriaRaw = primeiroItem?.categoria?.takeIf { it.isNotBlank() } ?: "OUTROS"
    val categoriaFormatada = categoriaRaw.lowercase().replaceFirstChar { it.uppercase() }
    val condicao =
        if (primeiroItem?.eNovo.equals("S", ignoreCase = true)) {
            "Novo"
        } else {
            "Usado"
        }

    return DoacaoDisponivel(
        id = (id ?: 0).toString(),
        itemTitle = titulo,
        quantidade = (primeiroItem?.quantidade ?: 1).coerceAtLeast(1),
        categoria = categoriaFormatada,
        condicao = condicao,
        descricao = primeiroItem?.descricao.orEmpty(),
        motivo = primeiroItem?.motivo.orEmpty(),
        nomeDoador = nomeDoador?.takeIf { it.isNotBlank() } ?: "Doador Solidário",
        urlImagem = urlImagem,
        doadorId = doadorId,
    )
}

@Serializable
data class RegistrarInteresseRequest(
    val mensagem: String? = null,
)

@Serializable
data class UsuarioInteressadoDto(
    val id: Int? = null,
    val nome: String? = null,
    val email: String? = null,
    val logradouro: String? = null,
    val bairro: String? = null,
    val numero: String? = null,
    val cep: String? = null,
    val mensagem: String? = null,
)

@Serializable
data class LocalDoacaoRequest(
    val nome: String,
    val logradouro: String? = null,
    val bairro: String? = null,
    val numero: String? = null,
    val cep: String? = null,
)

@Serializable
data class EfetivarDoacaoRequest(
    val donatarioId: Int,
    val localDoacao: LocalDoacaoRequest,
)

fun UsuarioInteressadoDto.toDomain(): Interessado {
    val idFinal = id ?: 0
    val endereco =
        listOfNotNull(
            logradouro?.takeIf { it.isNotBlank() },
            numero?.takeIf { it.isNotBlank() },
            bairro?.takeIf { it.isNotBlank() },
        ).joinToString(", ")
    return Interessado(
        id = idFinal,
        nome = nome?.takeIf { it.isNotBlank() } ?: "Interessado #$idFinal",
        email = email.orEmpty(),
        enderecoResumo = endereco,
        mensagem =
            mensagem?.takeIf { it.isNotBlank() }
                ?: "Demonstrou interesse em receber esta doação.",
    )
}

fun EfetivarDoacaoInput.toEfetivarRequest(): EfetivarDoacaoRequest =
    EfetivarDoacaoRequest(
        donatarioId = donatarioId,
        localDoacao =
            LocalDoacaoRequest(
                nome = nomeLocal.trim(),
                logradouro = logradouro.trim().ifBlank { null },
                bairro = bairro.trim().ifBlank { null },
                numero = numero.trim().ifBlank { null },
                cep = cep.filter { it.isDigit() }.ifBlank { null },
            ),
    )
