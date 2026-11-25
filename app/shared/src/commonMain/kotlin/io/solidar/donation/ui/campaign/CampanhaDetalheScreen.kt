package io.solidar.donation.ui.campaign

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
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
    isFavorito: Boolean = false,
    onToggleFavorito: () -> Unit = {},
    ehOrganizador: Boolean = false,
    onFinalizarCampanha: (() -> Unit)? = null,
    onCancelarCampanha: (() -> Unit)? = null,
    cancelandoCampanha: Boolean = false,
    erroCancelamento: String? = null,
) {
    var confirmarCancelamento by rememberSaveable { mutableStateOf(false) }
    var confirmarFinalizacao by rememberSaveable { mutableStateOf(false) }

    if (confirmarFinalizacao && onFinalizarCampanha != null) {
        AlertDialog(
            onDismissRequest = { confirmarFinalizacao = false },
            title = { Text("Finalizar campanha?") },
            text = {
                Text(
                    "Esta campanha será encerrada e removida da lista de campanhas abertas, " +
                        "mantendo todas as doações já recebidas.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarFinalizacao = false
                        onFinalizarCampanha()
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = "Confirmar finalização",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmarFinalizacao = false },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Voltar")
                }
            },
        )
    }

    if (confirmarCancelamento && onCancelarCampanha != null) {
        AlertDialog(
            onDismissRequest = { confirmarCancelamento = false },
            title = { Text("Cancelar campanha?") },
            text = {
                Text(
                    "Esta campanha será cancelada e os itens ou contribuições vinculados serão devolvidos. " +
                        "Deseja continuar?",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarCancelamento = false
                        onCancelarCampanha()
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = "Confirmar cancelamento",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmarCancelamento = false },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Voltar")
                }
            },
        )
    }
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
                        IconButton(
                            onClick = onToggleFavorito,
                            modifier =
                                Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isFavorito) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        },
                                    ),
                        ) {
                            Icon(
                                imageVector = AppIcons.Favorite,
                                contentDescription =
                                    if (isFavorito) {
                                        "Remover campanha dos favoritos"
                                    } else {
                                        "Adicionar campanha aos favoritos"
                                    },
                                tint =
                                    if (isFavorito) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        if (ehOrganizador) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (!erroCancelamento.isNullOrBlank()) {
                                    Text(
                                        text = erroCancelamento,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                                val campanhaAtiva = campaign.status.equals("ATIVA", ignoreCase = true)
                                val labelOrganizador =
                                    when (campaign.status.uppercase()) {
                                        "FINALIZADA" -> "Campanha finalizada"
                                        "CANCELADA" -> "Campanha cancelada"
                                        else -> "Você é o organizador desta campanha"
                                    }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = labelOrganizador,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    )
                                }
                                if (campanhaAtiva && (onFinalizarCampanha != null || onCancelarCampanha != null)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        if (onFinalizarCampanha != null) {
                                            Button(
                                                onClick = { confirmarFinalizacao = true },
                                                enabled = !cancelandoCampanha,
                                                shape = RoundedCornerShape(12.dp),
                                                colors =
                                                    ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                                    ),
                                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                            ) {
                                                Text(
                                                    text = "Finalizar campanha",
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                        }
                                        if (onCancelarCampanha != null) {
                                            OutlinedButton(
                                                onClick = { confirmarCancelamento = true },
                                                enabled = !cancelandoCampanha,
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                colors =
                                                    ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.error,
                                                    ),
                                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                            ) {
                                                if (cancelandoCampanha) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(18.dp),
                                                        strokeWidth = 2.dp,
                                                        color = MaterialTheme.colorScheme.error,
                                                    )
                                                } else {
                                                    Text(
                                                        text = "Cancelar campanha",
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = onQueroDoar,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                            ) {
                                Text("Quero doar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
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
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clearAndSetSemantics {},
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
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
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = "Organizado por ${campaign.organizer}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val percentualArrecadado = (campaign.progress * 100).toInt()
                    LinearProgressIndicator(
                        progress = { campaign.progress },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .semantics {
                                    contentDescription = "Progresso da campanha: $percentualArrecadado por cento"
                                },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Text(
                        text = "${formatBrl(campaign.raisedInCents)} arrecadados de ${formatBrl(campaign.goalInCents)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                    )

                    if (campaign.descricao.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sobre a campanha:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.semantics { heading() },
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
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.semantics { heading() },
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
        modifier = modifier.semantics(mergeDescendants = true) {},
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
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clearAndSetSemantics {},
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.nome.take(1).uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
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

@Preview
@Composable
private fun CampanhaDetalheDarkPreview() {
    DonationTheme(darkTheme = true) {
        CampanhaDetalheScreen(campaign = sampleCampaigns.first(), onBack = {}, onQueroDoar = {})
    }
}
