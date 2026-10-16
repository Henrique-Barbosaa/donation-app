package io.solidar.donation.ui.donation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.solidar.donation.model.Campaign
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.ui.home.sampleCampaigns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpcoesDoacaoScreen(
    campaign: Campaign,
    onBack: () -> Unit,
    onDoarDinheiro: () -> Unit,
    onDoarItens: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Tipo de Doação", modifier = Modifier.semantics { heading() })
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Como gostaria de ajudar a campanha?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = campaign.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF006B4E),
                fontWeight = FontWeight.Bold,
            )

            OpcaoDoacaoCard(
                titulo = "Contribuir com Dinheiro",
                descricao = "Ajude a instituição a bater a meta financeira através de Pix, Boleto ou Cartão.",
                emoji = "💰",
                onClick = onDoarDinheiro,
            )

            OpcaoDoacaoCard(
                titulo = "Doar Itens Físicos",
                descricao = "Tem os materiais solicitados? Agende a entrega ou recolha dos produtos.",
                emoji = "📦",
                onClick = onDoarItens,
            )
        }
    }
}

@Composable
private fun OpcaoDoacaoCard(
    titulo: String,
    descricao: String,
    emoji: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF006B4E).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF006B4E),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
private fun OpcoesDoacaoPreview() {
    DonationTheme {
        OpcoesDoacaoScreen(
            campaign = sampleCampaigns.first(),
            onBack = {},
            onDoarDinheiro = {},
            onDoarItens = {},
        )
    }
}
