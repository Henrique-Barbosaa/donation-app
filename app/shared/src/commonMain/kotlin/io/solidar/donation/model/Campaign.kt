package io.solidar.donation.model

data class CampaignItem(
    val id: String,
    val nome: String,
    val quantidadeDescricao: String,
    val iconeRef: String,
    val quantidadeAlvo: Int? = null,
)

data class Campaign(
    val id: String,
    val title: String,
    val organizer: String,
    val goalInCents: Long,
    val raisedInCents: Long,
    val descricao: String = "",
    val imageUrl: String? = null,
    val itensNecessarios: List<CampaignItem> = emptyList(),
    val instituicaoId: Int? = null,
    val status: String = "ATIVA",
) {
    val aceitaDinheiro: Boolean
        get() = goalInCents > 0L

    val aceitaItens: Boolean
        get() = itensNecessarios.isNotEmpty()

    val progress: Float
        get() =
            if (goalInCents <= 0L) {
                0f
            } else {
                (raisedInCents.toFloat() / goalInCents.toFloat()).coerceIn(0f, 1f)
            }
}
