package io.solidar.donation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.solidar.donation.model.Campaign
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.util.formatBrl

/**
 * Cartão reutilizável que apresenta o resumo de uma campanha.
 *
 * O componente não guarda estado: recebe a campanha a exibir e devolve o
 * clique para quem o chamou decidir o que fazer.
 */
@Composable
fun CampaignCard(
    campaign: Campaign,
    onClick: (Campaign) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick(campaign) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = campaign.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = campaign.organizer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { campaign.progress },
                modifier = Modifier.fillMaxWidth().height(6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatBrl(campaign.raisedInCents),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "meta ${formatBrl(campaign.goalInCents)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun CampaignCardPreview() {
    DonationTheme {
        CampaignCard(
            campaign = Campaign(
                id = "1",
                title = "Cestas básicas para o bairro das Rocas",
                organizer = "Instituto Mãos Solidárias",
                goalInCents = 500_000L,
                raisedInCents = 312_500L
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}