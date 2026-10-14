package io.solidar.donation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.solidar.donation.model.Campaign

@Composable
fun CampanhaDetalheScreen(
    campaign: Campaign?,
    onVoltar: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(onClick = onVoltar) {
            Text("Voltar")
        }

        if (campaign == null) {
            Text(
                text = "Campanha não encontrada.",
                style = MaterialTheme.typography.headlineSmall,
            )
        } else {
            Text(
                text = campaign.title,
                style = MaterialTheme.typography.headlineMedium,
            )

            Text("Organização: ${campaign.organizer}")

            Text(
                text = "Arrecadado: ${formatarValor(campaign.raisedInCents)}",
            )

            Text(
                text = "Meta: ${formatarValor(campaign.goalInCents)}",
            )

            LinearProgressIndicator(
                progress = { campaign.progress },
            )
        }
    }
}

private fun formatarValor(centavos: Long): String {
    val valor = centavos.coerceAtLeast(0L)
    val reais = valor / 100
    val centavosRestantes = valor % 100

    return "R$ $reais,${centavosRestantes.toString().padStart(2, '0')}"
}
