package io.solidar.donation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.solidar.donation.model.Campaign
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AvisoDadosDesatualizadosBanner
import io.solidar.donation.ui.components.CampaignCard
import io.solidar.donation.ui.components.EstadoCarregandoView
import io.solidar.donation.ui.components.EstadoErroView
import io.solidar.donation.ui.state.TelaEstado

/**
 * Tela de listagem de campanhas abertas, tratando exaustivamente os estados da classe selada
 * [TelaEstado] (`Carregando`, `Vazio`, `Sucesso` e `Erro`) via `when` sem `else`.
 */
@Composable
fun HomeScreen(
    estado: TelaEstado<List<Campaign>>,
    onCampaignClick: (Campaign) -> Unit,
    onTentarDeNovo: () -> Unit,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
    titulo: String = "Campanhas abertas",
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier =
                Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)
                    .semantics { heading() },
        )

        when (estado) {
            is TelaEstado.Carregando -> {
                EstadoCarregandoView(
                    descricao = "Carregando campanhas",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is TelaEstado.Vazio -> {
                EmptyCampaignsMessage(modifier = Modifier.fillMaxSize())
            }
            is TelaEstado.Erro -> {
                EstadoErroView(
                    mensagem = estado.mensagem,
                    onTentarDeNovo = onTentarDeNovo,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is TelaEstado.Sucesso -> {
                if (estado.desatualizado) {
                    AvisoDadosDesatualizadosBanner(onRecarregar = onTentarDeNovo)
                }
                CampaignListContent(
                    campaigns = estado.dados,
                    onCampaignClick = onCampaignClick,
                    wide = wide,
                )
            }
        }
    }
}

/** Sobrecarga conveniente para exibir uma lista direta já resolvida. */
@Composable
fun HomeScreen(
    campaigns: List<Campaign>,
    onCampaignClick: (Campaign) -> Unit,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
) {
    val estado =
        if (campaigns.isEmpty()) {
            TelaEstado.Vazio
        } else {
            TelaEstado.Sucesso(dados = campaigns)
        }
    HomeScreen(
        estado = estado,
        onCampaignClick = onCampaignClick,
        onTentarDeNovo = {},
        modifier = modifier,
        wide = wide,
    )
}

@Composable
private fun CampaignListContent(
    campaigns: List<Campaign>,
    onCampaignClick: (Campaign) -> Unit,
    wide: Boolean,
) {
    if (wide) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = campaigns, key = { it.id }) { campaign ->
                CampaignCard(
                    campaign = campaign,
                    onClick = onCampaignClick,
                )
            }
        }
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
            estado = TelaEstado.Sucesso(dados = sampleCampaigns),
            onCampaignClick = {},
            onTentarDeNovo = {},
        )
    }
}

@Preview(widthDp = 840, heightDp = 600)
@Composable
private fun HomeScreenWidePreview() {
    DonationTheme {
        HomeScreen(
            estado = TelaEstado.Sucesso(dados = sampleCampaigns),
            onCampaignClick = {},
            onTentarDeNovo = {},
            wide = true,
        )
    }
}

@Preview
@Composable
private fun HomeScreenEmptyPreview() {
    DonationTheme {
        HomeScreen(
            estado = TelaEstado.Vazio,
            onCampaignClick = {},
            onTentarDeNovo = {},
        )
    }
}

@Preview
@Composable
private fun HomeScreenErrorPreview() {
    DonationTheme(darkTheme = true) {
        HomeScreen(
            estado = TelaEstado.Erro("Sem conexão com o servidor. Verifique sua internet."),
            onCampaignClick = {},
            onTentarDeNovo = {},
        )
    }
}
