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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.solidar.donation.di.AppContainer
import io.solidar.donation.model.Interessado
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.ui.components.EstadoCarregandoView
import io.solidar.donation.ui.components.EstadoErroView
import io.solidar.donation.ui.state.TelaEstado

/**
 * Tela de efetivação de doação (issue #10): exibe interessados e permite confirmar entrega.
 */
@Composable
fun EfetivarDoacaoScreen(
    doacaoId: String,
    itemTitle: String,
    onNavigateBack: () -> Unit,
    onEfetivadaComSucesso: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EfetivarDoacaoViewModel = viewModel { AppContainer.default.createEfetivarDoacaoViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(doacaoId) {
        viewModel.carregarInteressados(doacaoId)
    }

    LaunchedEffect(uiState.envioState) {
        if (uiState.envioState is EfetivarEnvioState.Success) {
            viewModel.resetEnvio()
            onEfetivadaComSucesso()
        }
    }

    EfetivarDoacaoContent(
        itemTitle = itemTitle,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRecarregar = { viewModel.carregarInteressados(doacaoId) },
        onSelecionarInteressado = viewModel::selecionarInteressado,
        onNomeLocalChange = viewModel::onNomeLocalChange,
        onLogradouroChange = viewModel::onLogradouroChange,
        onBairroChange = viewModel::onBairroChange,
        onConfirmar = { viewModel.confirmarEfetivacao(doacaoId) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EfetivarDoacaoContent(
    itemTitle: String,
    uiState: EfetivarDoacaoUiState,
    onNavigateBack: () -> Unit,
    onRecarregar: () -> Unit,
    onSelecionarInteressado: (Int) -> Unit,
    onNomeLocalChange: (String) -> Unit,
    onLogradouroChange: (String) -> Unit,
    onBairroChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Efetivar Doação",
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
                    .background(MaterialTheme.colorScheme.background),
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Item a efetivar",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = itemTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            when (val estado = uiState.estadoInteressados) {
                is TelaEstado.Carregando -> {
                    EstadoCarregandoView(
                        descricao = "Carregando interessados",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                is TelaEstado.Vazio -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text =
                                "Nenhum interessado registrado ainda.\n" +
                                    "Quando alguém demonstrar interesse em Início (Oferecem doações) ou Explorar, aparecerá aqui.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                is TelaEstado.Erro -> {
                    EstadoErroView(
                        mensagem = estado.mensagem,
                        onTentarDeNovo = onRecarregar,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                is TelaEstado.Sucesso -> {
                    EfetivarFormBody(
                        interessados = estado.dados,
                        uiState = uiState,
                        onSelecionarInteressado = onSelecionarInteressado,
                        onNomeLocalChange = onNomeLocalChange,
                        onLogradouroChange = onLogradouroChange,
                        onBairroChange = onBairroChange,
                        onConfirmar = onConfirmar,
                    )
                }
            }
        }
    }
}

@Composable
private fun EfetivarFormBody(
    interessados: List<Interessado>,
    uiState: EfetivarDoacaoUiState,
    onSelecionarInteressado: (Int) -> Unit,
    onNomeLocalChange: (String) -> Unit,
    onLogradouroChange: (String) -> Unit,
    onBairroChange: (String) -> Unit,
    onConfirmar: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val envio = uiState.envioState
        if (envio is EfetivarEnvioState.Error) {
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

        Text(
            text = "Interessados nesta doação (${interessados.size}) *",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )

        Text(
            text = "Veja a mensagem de cada pessoa e selecione quem receberá o item.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        interessados.forEach { pessoa ->
            val selecionado = uiState.interessadoSelecionadoId == pessoa.id
            InteressadoOptionCard(
                interessado = pessoa,
                selecionado = selecionado,
                onClick = { onSelecionarInteressado(pessoa.id) },
            )
        }

        Text(
            text = "Local de entrega *",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )

        OutlinedTextField(
            value = uiState.nomeLocal,
            onValueChange = onNomeLocalChange,
            label = { Text("Nome do local de entrega") },
            placeholder = { Text("Ex: Portaria do IMD, Praça cívica, Sede da ONG...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
        )

        OutlinedTextField(
            value = uiState.logradouro,
            onValueChange = onLogradouroChange,
            label = { Text("Rua / Logradouro (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
        )

        OutlinedTextField(
            value = uiState.bairro,
            onValueChange = onBairroChange,
            label = { Text("Bairro (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
        )

        Spacer(modifier = Modifier.height(8.dp))

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
            if (uiState.envioState is EfetivarEnvioState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .semantics { contentDescription = "Confirmando entrega da doação" },
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = "Confirmar entrega",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InteressadoOptionCard(
    interessado: Interessado,
    selecionado: Boolean,
    onClick: () -> Unit,
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
                .selectable(
                    selected = selecionado,
                    onClick = onClick,
                    role = Role.RadioButton,
                ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                        text = interessado.nome.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = interessado.nome,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (selecionado) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )
                if (interessado.email.isNotBlank()) {
                    Text(
                        text = interessado.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (interessado.enderecoResumo.isNotBlank()) {
                    Text(
                        text = interessado.enderecoResumo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val mensagemExibida =
                    interessado.mensagem.ifBlank {
                        "Demonstrou interesse em receber esta doação."
                    }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color =
                        if (selecionado) {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                ) {
                    Text(
                        text = "“$mensagemExibida”",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun EfetivarDoacaoPreview() {
    DonationTheme {
        EfetivarDoacaoContent(
            itemTitle = "Casaco de frio juvenil",
            uiState =
                EfetivarDoacaoUiState(
                    estadoInteressados =
                        TelaEstado.Sucesso(
                            listOf(
                                Interessado(1, "Carlos Lima", "carlos@exemplo.com", "Rocas"),
                                Interessado(2, "Maria Clara", "maria@exemplo.com", "Tirol"),
                            ),
                        ),
                    interessadoSelecionadoId = 1,
                    nomeLocal = "Portaria principal do IMD",
                ),
            onNavigateBack = {},
            onRecarregar = {},
            onSelecionarInteressado = {},
            onNomeLocalChange = {},
            onLogradouroChange = {},
            onBairroChange = {},
            onConfirmar = {},
        )
    }
}
