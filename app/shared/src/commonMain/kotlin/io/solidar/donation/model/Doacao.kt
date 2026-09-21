package io.solidar.donation.model

enum class DoacaoDirecao {
    FEITA,
    RECEBIDA
}

enum class DoacaoStatus {
    CONCLUIDA,
    PENDENTE,
    CANCELADA
}

data class Doacao(
    val id: String,
    val itemTitle: String,
    val counterpartName: String,
    val direction: DoacaoDirecao,
    val status: DoacaoStatus,
    val dateLabel: String
)