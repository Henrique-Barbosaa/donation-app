package io.solidar.donation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.model.DoacaoStatus
import io.solidar.donation.theme.DonationTheme

/**
 * Cartão reutilizável que resume uma doação do histórico do usuário.
 */
@Composable
fun DoacaoCard(
    doacao: Doacao,
    modifier: Modifier = Modifier,
    onEfetivarClick: ((Doacao) -> Unit)? = null,
    onCancelarClick: ((Doacao) -> Unit)? = null,
    cancelando: Boolean = false,
) {
    Card(
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = doacao.itemTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                StatusBadge(status = doacao.status)
            }

            Text(
                text = counterpartLabel(doacao.direction, doacao.counterpartName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = doacao.dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val pendenteFeita =
                doacao.direction == DoacaoDirecao.FEITA &&
                    doacao.status == DoacaoStatus.PENDENTE

            if (pendenteFeita && onEfetivarClick != null) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { onEfetivarClick(doacao) },
                    enabled = !cancelando,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text(
                        text = "Ver interessados e efetivar",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (pendenteFeita && onCancelarClick != null) {
                OutlinedButton(
                    onClick = { onCancelarClick(doacao) },
                    enabled = !cancelando,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                ) {
                    if (cancelando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        Text(
                            text = "Cancelar doação",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

private fun counterpartLabel(
    direction: DoacaoDirecao,
    counterpartName: String,
): String =
    when (direction) {
        DoacaoDirecao.FEITA -> "Para: $counterpartName"
        DoacaoDirecao.RECEBIDA -> "De: $counterpartName"
    }

@Composable
private fun StatusBadge(
    status: DoacaoStatus,
    modifier: Modifier = Modifier,
) {
    val (label, containerColor, contentColor) =
        when (status) {
            DoacaoStatus.CONCLUIDA ->
                Triple(
                    "Concluída",
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                )
            DoacaoStatus.PENDENTE ->
                Triple(
                    "Pendente",
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer,
                )
            DoacaoStatus.CANCELADA ->
                Triple(
                    "Cancelada",
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer,
                )
        }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Preview
@Composable
private fun DoacaoCardFeitaPreview() {
    DonationTheme {
        DoacaoCard(
            doacao =
                Doacao(
                    id = "1",
                    itemTitle = "Cesta básica",
                    counterpartName = "Instituição Parceira",
                    direction = DoacaoDirecao.FEITA,
                    status = DoacaoStatus.CONCLUIDA,
                    dateLabel = "12 de setembro de 2026",
                ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun DoacaoCardRecebidaPendentePreview() {
    DonationTheme {
        DoacaoCard(
            doacao =
                Doacao(
                    id = "2",
                    itemTitle = "Material escolar",
                    counterpartName = "Maria da Silva",
                    direction = DoacaoDirecao.RECEBIDA,
                    status = DoacaoStatus.PENDENTE,
                    dateLabel = "18 de setembro de 2026",
                ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun DoacaoCardDarkPreview() {
    DonationTheme(darkTheme = true) {
        DoacaoCard(
            doacao =
                Doacao(
                    id = "3",
                    itemTitle = "Cesta básica",
                    counterpartName = "Instituição Parceira",
                    direction = DoacaoDirecao.FEITA,
                    status = DoacaoStatus.CONCLUIDA,
                    dateLabel = "12 de setembro de 2026",
                ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
