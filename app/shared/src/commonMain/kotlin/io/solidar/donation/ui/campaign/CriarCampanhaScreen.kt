package io.solidar.donation.ui.campaign

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.KeyboardType
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
import io.solidar.donation.util.LocalColetaForm
import io.solidar.donation.util.Validators
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

            CardFotoCapaCampanha()

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

            SecaoModalidadesCampanha(
                form = form,
                erroTipo = erros.tipoArrecadacao,
                erroMetaDinheiro = erros.metaDinheiro,
                onFormChange = onFormChange,
            )

            if (form.pedirItens) {
                SecaoItensCampanha(
                    form = form,
                    erroItens = erros.itens,
                    onFormChange = onFormChange,
                )
            }

            SecaoLocaisColeta(
                form = form,
                erroLocais = erros.locais,
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
private fun CardFotoCapaCampanha() {
    var exibiuAvisoUpload by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable(role = Role.Button) { exibiuAvisoUpload = !exibiuAvisoUpload },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Camera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Foto de capa da campanha",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text =
                    if (exibiuAvisoUpload) {
                        "O envio de fotos pela galeria/câmera será liberado na próxima versão."
                    } else {
                        "Toque para adicionar uma foto de capa (opcional)."
                    },
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (exibiuAvisoUpload) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SecaoModalidadesCampanha(
    form: CampanhaForm,
    erroTipo: String?,
    erroMetaDinheiro: String?,
    onFormChange: (CampanhaForm) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "O que esta campanha vai arrecadar? *",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )

        ModalidadeToggleCard(
            titulo = "Pedir objetos / itens físicos",
            subtitulo = "Permite listar os itens necessários e a quantidade de cada um.",
            checked = form.pedirItens,
            switchDescription = "Ativar pedido de objetos",
            onCheckedChange = { ativo -> onFormChange(form.copy(pedirItens = ativo)) },
        )

        ModalidadeToggleCard(
            titulo = "Pedir arrecadação em dinheiro (PIX)",
            subtitulo = "Define uma meta financeira real (inicia em R$ 0,00 arrecadados).",
            checked = form.pedirDinheiro,
            switchDescription = "Ativar pedido de dinheiro",
            onCheckedChange = { ativo ->
                onFormChange(
                    form.copy(
                        pedirDinheiro = ativo,
                        metaReaisTexto = if (ativo) form.metaReaisTexto else "",
                    ),
                )
            },
        )

        if (form.pedirDinheiro) {
            val showMetaError = erroMetaDinheiro != null && form.metaReaisTexto.isNotBlank()
            OutlinedTextField(
                value = form.metaReaisTexto,
                onValueChange = { novo ->
                    val digitos = novo.filter { it.isDigit() }.take(7)
                    onFormChange(form.copy(metaReaisTexto = digitos))
                },
                label = { Text("Meta financeira (em reais) *") },
                placeholder = { Text("Ex: 500") },
                prefix = { Text("R$ ") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = showMetaError,
                supportingText = {
                    if (showMetaError) {
                        Text(
                            text = erroMetaDinheiro.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        Text(
                            text = "A campanha começa com R$ 0,00 arrecadados e atualiza a cada contribuição.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Number,
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

        if (erroTipo != null) {
            Text(
                text = erroTipo,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ModalidadeToggleCard(
    titulo: String,
    subtitulo: String,
    checked: Boolean,
    switchDescription: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (checked) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color =
                        if (checked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    shape = RoundedCornerShape(16.dp),
                ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.semantics { contentDescription = switchDescription },
            )
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

@Composable
private fun SecaoLocaisColeta(
    form: CampanhaForm,
    erroLocais: String?,
    onFormChange: (CampanhaForm) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Locais físicos de coleta (opcional)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )

        Text(
            text = "Informe pontos físicos onde os doadores podem entregar os itens presencialmente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        form.locais.forEachIndexed { index, local ->
            LocalColetaFormCard(
                index = index,
                local = local,
                onLocalChange = { atualizado ->
                    val listaAtualizada =
                        form.locais.map { atual ->
                            if (atual.id == local.id) atualizado else atual
                        }
                    onFormChange(form.copy(locais = listaAtualizada))
                },
                onRemover = {
                    onFormChange(form.copy(locais = form.locais.filterNot { it.id == local.id }))
                },
            )
        }

        if (erroLocais != null) {
            Text(
                text = erroLocais,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        OutlinedButton(
            onClick = {
                val novoLocal = LocalColetaForm(id = form.proximoLocalId())
                onFormChange(form.copy(locais = form.locais + novoLocal))
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
                text = "Adicionar local de coleta",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun LocalColetaFormCard(
    index: Int,
    local: LocalColetaForm,
    onLocalChange: (LocalColetaForm) -> Unit,
    onRemover: () -> Unit,
) {
    val numeroLocal = index + 1
    val fieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedContainerColor = MaterialTheme.colorScheme.background,
        )

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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = local.nome,
                    onValueChange = { onLocalChange(local.copy(nome = it.take(Validators.NOME_LOCAL_MAX))) },
                    label = { Text("Nome do local $numeroLocal *") },
                    placeholder = { Text("Ex: Sede da ONG, Portaria do IMD") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                    colors = fieldColors,
                )

                IconButton(
                    onClick = onRemover,
                    modifier =
                        Modifier
                            .size(48.dp)
                            .semantics { contentDescription = "Remover local de coleta $numeroLocal" },
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

            OutlinedTextField(
                value = local.logradouro,
                onValueChange = { onLocalChange(local.copy(logradouro = it.take(100))) },
                label = { Text("Logradouro") },
                placeholder = { Text("Ex: Av. Senador Salgado Filho") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                colors = fieldColors,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = local.bairro,
                    onValueChange = { onLocalChange(local.copy(bairro = it.take(100))) },
                    label = { Text("Bairro") },
                    placeholder = { Text("Ex: Lagoa Nova") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next,
                        ),
                    colors = fieldColors,
                )

                OutlinedTextField(
                    value = local.numero,
                    onValueChange = { onLocalChange(local.copy(numero = it.take(10))) },
                    label = { Text("Nº") },
                    placeholder = { Text("120") },
                    modifier = Modifier.width(88.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = fieldColors,
                )
            }

            OutlinedTextField(
                value = local.cep,
                onValueChange = { novo ->
                    onLocalChange(local.copy(cep = novo.filter { it.isDigit() }.take(8)))
                },
                label = { Text("CEP (opcional)") },
                placeholder = { Text("Somente números (8 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                colors = fieldColors,
            )
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
