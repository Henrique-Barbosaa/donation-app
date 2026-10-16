package io.solidar.donation.ui.campaign

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.CampaignItem
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.ui.home.sampleCampaigns
import io.solidar.donation.util.formatBrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampanhaDetalheScreen(
    campaign: Campaign?,
    onBack: () -> Unit,
    onQueroDoar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Campanha", modifier = Modifier.semantics { heading() }) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        bottomBar = {
            if (campaign != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val interesseSalvo = io.solidar.donation.util.FavoritosManager.isFavorito(campaign.id)

                        IconButton(
                            onClick = { io.solidar.donation.util.FavoritosManager.toggle(campaign.id) },
                            modifier =
                                Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        ) {
                            Text(text = if (interesseSalvo) "❤️" else "🤍", fontSize = 28.sp)
                        }
                        Button(
                            onClick = onQueroDoar,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF006B4E),
                                    contentColor = Color.White,
                                ),
                        ) {
                            Text("Quero doar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
    ) { padding ->
        if (campaign == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Campanha não encontrada.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF006B4E).copy(alpha = 0.2f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🖼️", fontSize = 48.sp)
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = campaign.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = "Organizado por ${campaign.organizer}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { campaign.progress },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFF006B4E),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Text(
                        text = "${formatBrl(campaign.raisedInCents)} arrecadados de ${formatBrl(campaign.goalInCents)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF006B4E),
                        fontWeight = FontWeight.ExtraBold,
                    )

                    if (campaign.descricao.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sobre a campanha:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = campaign.descricao,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (campaign.itensNecessarios.isNotEmpty()) {
                        Text(
                            text = "Itens necessários para doar:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        campaign.itensNecessarios.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                rowItems.forEach { item ->
                                    CampaignItemCard(item = item, modifier = Modifier.weight(1f))
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignItemCard(
    item: CampaignItem,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.nome.take(1).uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = item.nome,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.quantidadeDescricao,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
private fun CampanhaDetalhePreview() {
    DonationTheme {
        CampanhaDetalheScreen(campaign = sampleCampaigns.first(), onBack = {}, onQueroDoar = {})
    }
}
