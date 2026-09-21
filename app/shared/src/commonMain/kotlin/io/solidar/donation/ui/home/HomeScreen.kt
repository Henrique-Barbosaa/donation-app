package io.solidar.donation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.solidar.donation.model.Campaign
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.CampaignCard

/**
 * Lista as campanhas abertas.
 *
 * A tela é stateless por opção de projeto: recebe pronto o que deve mostrar e
 * devolve os eventos para o chamador. Assim ela pode ser renderizada em
 * `@Preview` e testada sem depender de ViewModel ou de rede.
 */
@Composable
fun HomeScreen(
    campaigns: List<Campaign>,
    onCampaignClick: (Campaign) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Campanhas abertas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
        )

        if (campaigns.isEmpty()) {
            EmptyCampaignsMessage(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = campaigns, key = { it.id }) { campaign ->
                    CampaignCard(
                        campaign = campaign,
                        onClick = onCampaignClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCampaignsMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Nenhuma campanha por aqui ainda.\nAssim que alguém criar uma, ela aparece nesta lista.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun HomeScreenFilledPreview() {
    DonationTheme {
        HomeScreen(
            campaigns = sampleCampaigns,
            onCampaignClick = {},
        )
    }
}

@Preview
@Composable
private fun HomeScreenEmptyPreview() {
    DonationTheme {
        HomeScreen(
            campaigns = emptyList(),
            onCampaignClick = {},
        )
    }
}
