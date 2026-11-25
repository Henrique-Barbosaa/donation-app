package io.solidar.donation.ui.donation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.solidar.donation.di.AppContainer
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.CampaignItem
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.ui.home.sampleCampaigns

/**
 * Tela para o usuário selecionar os itens solicitados por uma campanha que ainda não foram doados.
 */
@Composable
fun DoarItensCampanhaScreen(
    campaign: Campaign,
    onNavigateBack: () -> Unit,
    onDoacaoConcluida: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DoarItensCampanhaViewModel = viewModel { AppContainer.default.createDoarItensCampanhaViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(campaign.id) {
        viewModel.carregarItensPendentes(campaign)
    }

    LaunchedEffect(uiState.envioState) {
        if (uiState.envioState is DoarItensEnvioState.Success) {
            viewModel.resetEnvio()
            onDoacaoConcluida()
        }
    }

    DoarItensCampanhaContent(
        campaign = campaign,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onAlternarItem = viewModel::alternarSelecaoItem,
        onAlterarQuantidade = viewModel::alterarQuantidade,
        onObservacaoChange = viewModel::onObservacaoChange,
        onConfirmar = { viewModel.confirmarDoacao(campaign) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoarItensCampanhaContent(
    campaign: Campaign,
    uiState: DoarItensCampanhaUiState,
    onNavigateBack: () -> Unit,
    onAlternarItem: (String) -> Unit,
    onAlterarQuantidade: (String, Int) -> Unit,
    onObservacaoChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Selecionar Itens",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = campaign.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "Organizado por ${campaign.organizer}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            val envio = uiState.envioState
            if (envio is DoarItensEnvioState.Error) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = envio.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            if (uiState.itensPendentes.isEmpty()) {
                val mensagemVazio =
                    if (campaign.itensNecessarios.isEmpty()) {
                        "Esta campanha não listou itens físicos específicos.\n" +
                            "Você ainda pode ajudar contribuindo via PIX ou explorando outras campanhas."
                    } else {
                        "Todos os itens solicitados por esta campanha já foram doados!\n" +
                            "Você ainda pode ajudar contribuindo via PIX ou explorando outras campanhas."
                    }
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = mensagemVazio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Text(
                    text = "Itens ainda disponíveis para doar *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )

                Text(
                    text = "Toque nos itens que você deseja doar e ajuste a quantidade.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                uiState.itensPendentes.forEach { item ->
                    val quantidade = uiState.quantidadesSelecionadas[item.id]
                    ItemCampanhaSelecionavelCard(
                        item = item,
                        selecionado = quantidade != null,
                        quantidade = quantidade ?: 1,
                        onToggle = { onAlternarItem(item.id) },
                        onDecrement = { onAlterarQuantidade(item.id, -1) },
                        onIncrement = { onAlterarQuantidade(item.id, 1) },
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Observação para a entrega (opcional)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() },
                    )
                    OutlinedTextField(
                        value = uiState.observacao,
                        onValueChange = onObservacaoChange,
                        placeholder = { Text("Ex: Posso entregar no sábado pela manhã...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2,
                        keyboardOptions =
                            KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done,
                            ),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                            ),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val textoBotao =
                    if (uiState.totalItensSelecionados > 0) {
                        "Confirmar doação (${uiState.totalItensSelecionados})"
                    } else {
                        "Confirmar doação"
                    }

                Button(
                    onClick = onConfirmar,
                    enabled = uiState.podeConfirmar,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                ) {
                    if (uiState.envioState is DoarItensEnvioState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier =
                                Modifier
                                    .size(24.dp)
                                    .semantics { contentDescription = "Registrando doação de itens" },
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = textoBotao,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ItemCampanhaSelecionavelCard(
    item: CampaignItem,
    selecionado: Boolean,
    quantidade: Int,
    onToggle: () -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (selecionado) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .border(
                    width = if (selecionado) 1.5.dp else 1.dp,
                    color =
                        if (selecionado) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    shape = RoundedCornerShape(16.dp),
                )
                .toggleable(
                    value = selecionado,
                    onValueChange = { onToggle() },
                    role = Role.Checkbox,
                ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (selecionado) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selecionado) {
                        Icon(
                            imageVector = AppIcons.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Text(
                            text = item.nome.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nome,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color =
                            if (selecionado) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                    Text(
                        text = item.quantidadeDescricao,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (selecionado) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Quantidade que vou doar",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        IconButton(
                            onClick = onDecrement,
                            enabled = quantidade > 1,
                            modifier =
                                Modifier
                                    .size(48.dp)
                                    .semantics { contentDescription = "Diminuir quantidade de ${item.nome}" },
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "$quantidade",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.Center,
                        )
                        IconButton(
                            onClick = onIncrement,
                            modifier =
                                Modifier
                                    .size(48.dp)
                                    .semantics { contentDescription = "Aumentar quantidade de ${item.nome}" },
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DoarItensCampanhaPreview() {
    DonationTheme {
        val campanha = sampleCampaigns.first()
        DoarItensCampanhaContent(
            campaign = campanha,
            uiState =
                DoarItensCampanhaUiState(
                    itensPendentes = campanha.itensNecessarios.take(3),
                    quantidadesSelecionadas = mapOf(campanha.itensNecessarios.first().id to 2),
                ),
            onNavigateBack = {},
            onAlternarItem = {},
            onAlterarQuantidade = { _, _ -> },
            onObservacaoChange = {},
            onConfirmar = {},
        )
    }
}
