package io.solidar.donation.domain.model

/**
 * Item individual solicitado em uma campanha de doação, com sua quantidade unitária alvo.
 */
data class ItemCampanhaInput(
    val nome: String,
    val quantidade: Int = 1,
)

/**
 * Local físico de coleta associado a uma campanha de arrecadação.
 */
data class LocalColetaInput(
    val nome: String,
    val logradouro: String = "",
    val bairro: String = "",
    val numero: String = "",
    val cep: String = "",
)

/**
 * Entrada pura da camada de domínio para criação de uma nova campanha de doação.
 */
data class NovaCampanhaInput(
    val titulo: String,
    val descricao: String,
    val itens: List<ItemCampanhaInput> = emptyList(),
    val metaEmCentavos: Long = 0L,
    val metaVoluntarios: Int = itens.sumOf { it.quantidade.coerceAtLeast(1) },
    val locais: List<LocalColetaInput> = emptyList(),
) {
    val itensFocoFormatado: String
        get() =
            itens
                .filter { it.nome.isNotBlank() }
                .joinToString(", ") { item ->
                    "${item.nome.trim()} (${item.quantidade.coerceAtLeast(1)} un.)"
                }
}
