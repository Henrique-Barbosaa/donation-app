package io.solidar.donation.ui.campaign

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedButton
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
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.util.CampanhaForm
import io.solidar.donation.util.ItemCampanhaForm
import io.solidar.donation.util.validarCampanha

/**
 * Tela de criação de nova campanha de doação (para contas de Instituição),
 * com definição unitária de quantidade para cada objeto solicitado.
 */
@Composable
fun CriarCampanhaScreen(
    onNavigateBack: () -> Unit,
    onCampaignCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CriarCampanhaViewModel = viewModel { AppContainer.default.createCriarCampanhaViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.envioState) {
        if (uiState.envioState is CriarCampanhaState.Success) {
            viewModel.resetState()
            onCampaignCreated()
        }
    }

    CriarCampanhaContent(
        state = uiState.envioState,
        form = uiState.form,
        onFormChange = viewModel::onFormChange,
        onNavigateBack = {
            viewModel.resetState()
            onNavigateBack()
        },
        onSubmit = viewModel::criarCampanha,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarCampanhaContent(
    state: CriarCampanhaState,
    form: CampanhaForm,
    onFormChange: (CampanhaForm) -> Unit,
    onNavigateBack: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val erros = validarCampanha(form)
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nova Campanha",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (state is CriarCampanhaState.Error) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Título da campanha *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )

                val showTituloError = erros.titulo != null && form.titulo.isNotBlank()

                OutlinedTextField(
                    value = form.titulo,
                    onValueChange = { onFormChange(form.copy(titulo = it)) },
                    placeholder = { Text("Ex: Cestas básicas para o bairro das Rocas...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = showTituloError,
                    supportingText = {
                        if (showTituloError) {
                            Text(
                                text = erros.titulo.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    },
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
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Descrição da campanha",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )

                val showDescError = erros.descricao != null

                OutlinedTextField(
                    value = form.descricao,
                    onValueChange = { onFormChange(form.copy(descricao = it)) },
                    placeholder = { Text("Explique a causa, quantas famílias serão apoiadas e como doar...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    isError = showDescError,
                    supportingText = {
                        if (showDescError) {
                            Text(
                                text = erros.descricao.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    },
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
            }

            SecaoItensCampanha(
                form = form,
                erroItens = erros.itens,
                onFormChange = onFormChange,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val isSubmitting = state is CriarCampanhaState.Loading

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = erros.valido && !isSubmitting,
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
                                .semantics { contentDescription = "Publicando campanha" },
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Publicar Campanha",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SecaoItensCampanha(
    form: CampanhaForm,
    erroItens: String?,
    onFormChange: (CampanhaForm) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Itens da campanha *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            if (form.totalUnidades > 0) {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Total: ${form.totalUnidades} un.",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        Text(
            text = "Defina cada objeto solicitado e a quantidade necessária individualmente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        form.itens.forEachIndexed { index, item ->
            ItemCampanhaFormCard(
                index = index,
                item = item,
                podeRemover = form.itens.size > 1,
                onNomeChange = { novoNome ->
                    val atualizados =
                        form.itens.map { atual ->
                            if (atual.id == item.id) atual.copy(nome = novoNome) else atual
                        }
                    onFormChange(form.copy(itens = atualizados))
                },
                onQuantidadeChange = { novaQtd ->
                    val atualizados =
                        form.itens.map { atual ->
                            if (atual.id == item.id) atual.copy(quantidade = novaQtd.coerceAtLeast(1)) else atual
                        }
                    onFormChange(form.copy(itens = atualizados))
                },
                onRemover = {
                    val restantes = form.itens.filterNot { it.id == item.id }
                    if (restantes.isNotEmpty()) {
                        onFormChange(form.copy(itens = restantes))
                    }
                },
            )
        }

        if (erroItens != null && form.itensPreenchidos.isNotEmpty()) {
            Text(
                text = erroItens,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        OutlinedButton(
            onClick = {
                val novoItem = ItemCampanhaForm(id = form.proximoItemId(), nome = "", quantidade = 1)
                onFormChange(form.copy(itens = form.itens + novoItem))
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        ) {
            Icon(
                imageVector = AppIcons.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Adicionar outro item",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ItemCampanhaFormCard(
    index: Int,
    item: ItemCampanhaForm,
    podeRemover: Boolean,
    onNomeChange: (String) -> Unit,
    onQuantidadeChange: (Int) -> Unit,
    onRemover: () -> Unit,
) {
    val numeroItem = index + 1
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier =
            Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(16.dp),
                ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = item.nome,
                    onValueChange = onNomeChange,
                    placeholder = { Text("Item $numeroItem (ex: Arroz, Cobertor, Caderno)") },
                    modifier = Modifier.weight(1f),
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
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                        ),
                )

                if (podeRemover) {
                    IconButton(
                        onClick = onRemover,
                        modifier =
                            Modifier
                                .size(48.dp)
                                .semantics { contentDescription = "Remover item $numeroItem" },
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "×",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Quantidade necessária",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        onClick = { onQuantidadeChange(item.quantidade - 1) },
                        enabled = item.quantidade > 1,
                        modifier =
                            Modifier
                                .size(48.dp)
                                .semantics { contentDescription = "Diminuir quantidade do item $numeroItem" },
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (item.quantidade > 1) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "-",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color =
                                    if (item.quantidade > 1) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                    }

                    Text(
                        text = "${item.quantidade}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center,
                    )

                    IconButton(
                        onClick = { onQuantidadeChange(item.quantidade + 1) },
                        modifier =
                            Modifier
                                .size(48.dp)
                                .semantics { contentDescription = "Aumentar quantidade do item $numeroItem" },
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "+",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CriarCampanhaPreview() {
    DonationTheme {
        CriarCampanhaContent(
            state = CriarCampanhaState.Idle,
            form =
                CampanhaForm(
                    titulo = "Campanha do Agasalho 2026",
                    descricao = "Arrecadação de casacos e cobertores para famílias da zona leste.",
                    itens =
                        listOf(
                            ItemCampanhaForm(id = 1, nome = "Casacos", quantidade = 15),
                            ItemCampanhaForm(id = 2, nome = "Cobertores", quantidade = 10),
                        ),
                ),
            onFormChange = {},
            onNavigateBack = {},
            onSubmit = {},
        )
    }
}
