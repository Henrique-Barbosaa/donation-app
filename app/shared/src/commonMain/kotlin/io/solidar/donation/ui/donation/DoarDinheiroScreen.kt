package io.solidar.donation.ui.donation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.solidar.donation.di.AppContainer
import io.solidar.donation.model.Campaign
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.ui.home.sampleCampaigns
import io.solidar.donation.util.formatBrl

private val VALORES_PIX_SUGERIDOS = listOf(20, 50, 100, 200)

/**
 * Tela de contribuição financeira via PIX para uma campanha.
 */
@Composable
fun DoarDinheiroScreen(
    campaign: Campaign,
    onNavigateBack: () -> Unit,
    onContribuicaoConcluida: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DoarDinheiroViewModel = viewModel { AppContainer.default.createDoarDinheiroViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val chavePix = gerarChavePixCampanha(campaign)

    LaunchedEffect(uiState.envioState) {
        if (uiState.envioState is DoarDinheiroEnvioState.Success) {
            viewModel.resetEnvio()
            onContribuicaoConcluida()
        }
    }

    DoarDinheiroContent(
        campaign = campaign,
        chavePix = chavePix,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onValorChange = viewModel::onValorChange,
        onSelecionarValor = viewModel::selecionarValorSugerido,
        onCopiarChave = {
            clipboard.setText(AnnotatedString(chavePix))
            viewModel.marcarChaveCopiada()
        },
        onConfirmar = { viewModel.confirmarContribuicao(campaign) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoarDinheiroContent(
    campaign: Campaign,
    chavePix: String,
    uiState: DoarDinheiroUiState,
    onNavigateBack: () -> Unit,
    onValorChange: (String) -> Unit,
    onSelecionarValor: (Int) -> Unit,
    onCopiarChave: () -> Unit,
    onConfirmar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Contribuir com Dinheiro",
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
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
                        text = "Instituição: ${campaign.organizer}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            val envio = uiState.envioState
            if (envio is DoarDinheiroEnvioState.Error) {
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

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Escolha o valor da contribuição *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    VALORES_PIX_SUGERIDOS.forEach { valor ->
                        val selecionado = uiState.valorReaisTexto == valor.toString()
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        width = 1.dp,
                                        color =
                                            if (selecionado) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                    )
                                    .selectable(
                                        selected = selecionado,
                                        onClick = { onSelecionarValor(valor) },
                                        role = Role.RadioButton,
                                    ),
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "R$ $valor",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color =
                                        if (selecionado) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.valorReaisTexto,
                    onValueChange = onValorChange,
                    label = { Text("Outro valor (em reais)") },
                    prefix = { Text("R$ ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Number,
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

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Pagamento via PIX",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = "Chave PIX da campanha:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = chavePix,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    OutlinedButton(
                        onClick = onCopiarChave,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        Text(
                            text = if (uiState.chaveCopiada) "Chave PIX copiada ✓" else "Copiar chave PIX",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val isSubmitting = uiState.envioState is DoarDinheiroEnvioState.Loading
            Button(
                onClick = onConfirmar,
                enabled = uiState.valorValido && !isSubmitting,
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
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier =
                            Modifier
                                .size(24.dp)
                                .semantics { contentDescription = "Registrando contribuição via PIX" },
                        strokeWidth = 2.dp,
                    )
                } else {
                    val labelValor =
                        if (uiState.valorValido) {
                            "Confirmar contribuição (${formatBrl(uiState.valorEmCentavos)})"
                        } else {
                            "Confirmar contribuição"
                        }
                    Text(
                        text = labelValor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun gerarChavePixCampanha(campaign: Campaign): String {
    val slug =
        campaign.organizer
            .lowercase()
            .filter { it.isLetterOrDigit() }
            .take(18)
            .ifBlank { "campanha${campaign.id}" }
    return "$slug@solidar.io"
}

@Preview
@Composable
private fun DoarDinheiroPreview() {
    DonationTheme {
        DoarDinheiroContent(
            campaign = sampleCampaigns.first(),
            chavePix = "institutomaossolid@solidar.io",
            uiState = DoarDinheiroUiState(valorReaisTexto = "50"),
            onNavigateBack = {},
            onValorChange = {},
            onSelecionarValor = {},
            onCopiarChave = {},
            onConfirmar = {},
        )
    }
}
