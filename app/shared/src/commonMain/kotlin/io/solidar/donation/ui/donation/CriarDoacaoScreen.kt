package io.solidar.donation.ui.donation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.util.DoacaoForm
import io.solidar.donation.util.validarDoacao

val CATEGORIAS_PADRAO =
    listOf(
        "Roupas",
        "Alimentos",
        "Móveis",
        "Eletrônicos",
        "Livros",
        "Brinquedos",
        "Outros",
    )

/**
 * Tela de criação de uma nova doação com elevação de estado para o ViewModel.
 */
@Composable
fun CriarDoacaoScreen(
    onNavigateBack: () -> Unit,
    onDonationCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CriarDoacaoViewModel = viewModel { CriarDoacaoViewModel() },
) {
    val state by viewModel.state.collectAsState()
    var form by remember { mutableStateOf(DoacaoForm()) }

    LaunchedEffect(state) {
        if (state is CriarDoacaoState.Success) {
            viewModel.resetState()
            onDonationCreated()
        }
    }

    CriarDoacaoContent(
        state = state,
        form = form,
        onFormChange = { form = it },
        onNavigateBack = {
            viewModel.resetState()
            onNavigateBack()
        },
        onSubmit = {
            viewModel.criarDoacao(
                nome = form.nome,
                quantidade = form.quantidade,
                categoria = form.categoria,
                eNovo = form.eNovo,
                descricao = form.descricao,
                motivo = form.motivo,
            )
        },
        modifier = modifier,
    )
}

/**
 * Componente stateless de conteúdo da criação de doação.
 * A validação é pura derivada via [validarDoacao], sem `remember` extra.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarDoacaoContent(
    state: CriarDoacaoState,
    form: DoacaoForm,
    onFormChange: (DoacaoForm) -> Unit,
    onNavigateBack: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val erros = validarDoacao(form)
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nova Doação",
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
            if (state is CriarDoacaoState.Error) {
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

            // Seção 1: Upload / Foto do Item
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
                        .clickable { /* Ação futura: abrir galeria/câmera */ },
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
                            contentDescription = "Tirar Foto",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Foto da doação",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() },
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Adicione fotos para ajudar os interessados a verem os detalhes do item",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Seção 2: Nome do Item
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "O que você está doando? *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )

                val showNomeError = erros.nome != null && form.nome.isNotBlank()

                OutlinedTextField(
                    value = form.nome,
                    onValueChange = { onFormChange(form.copy(nome = it)) },
                    placeholder = { Text("Ex: Casaco de frio juvenil, Cesta básica...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = showNomeError,
                    supportingText = {
                        if (showNomeError) {
                            Text(
                                text = erros.nome,
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

            // Seção 3: Quantidade (Stepper + / -)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Quantidade de itens",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = "Mínimo 1 unidade",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        onClick = {
                            if (form.quantidade > 1) {
                                onFormChange(form.copy(quantidade = form.quantidade - 1))
                            }
                        },
                        enabled = form.quantidade > 1,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (form.quantidade > 1) {
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
                                    if (form.quantidade > 1) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                    }

                    Text(
                        text = "${form.quantidade}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center,
                    )

                    IconButton(
                        onClick = { onFormChange(form.copy(quantidade = form.quantidade + 1)) },
                        modifier = Modifier.size(48.dp),
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

            // Seção 4: Categoria (Chips com Scroll Horizontal)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Categoria *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CATEGORIAS_PADRAO.forEach { cat ->
                        val isSelected = form.categoria == cat
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(
                                        width = 1.dp,
                                        color =
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant
                                            },
                                        shape = RoundedCornerShape(20.dp),
                                    )
                                    .clickable { onFormChange(form.copy(categoria = cat)) },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = AppIcons.Check,
                                        contentDescription = "Selecionado",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                Text(
                                    text = cat,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }

            // Seção 5: Condição do Item (Novo vs. Usado)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Condição do item",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Opção Usado
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    if (!form.eNovo) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                            ),
                        modifier =
                            Modifier
                                .weight(1f)
                                .border(
                                    width = if (!form.eNovo) 1.5.dp else 1.dp,
                                    color =
                                        if (!form.eNovo) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                )
                                .clickable { onFormChange(form.copy(eNovo = false)) },
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Usado",
                                fontWeight = FontWeight.Bold,
                                color =
                                    if (!form.eNovo) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                            )
                            Text(
                                text = "Em bom estado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Opção Novo
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    if (form.eNovo) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                            ),
                        modifier =
                            Modifier
                                .weight(1f)
                                .border(
                                    width = if (form.eNovo) 1.5.dp else 1.dp,
                                    color =
                                        if (form.eNovo) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                )
                                .clickable { onFormChange(form.copy(eNovo = true)) },
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Novo",
                                fontWeight = FontWeight.Bold,
                                color =
                                    if (form.eNovo) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                            )
                            Text(
                                text = "Na embalagem/etiqueta",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Seção 6: Descrição Detalhada
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Descrição do item",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )

                val showDescError = erros.descricao != null

                OutlinedTextField(
                    value = form.descricao,
                    onValueChange = { onFormChange(form.copy(descricao = it)) },
                    placeholder = { Text("Descreva tamanho, cor, funcionamento ou outros detalhes relevantes...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    isError = showDescError,
                    supportingText = {
                        if (showDescError) {
                            Text(
                                text = erros.descricao,
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

            // Seção 7: Motivo da doação
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Motivo da doação (opcional)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )

                OutlinedTextField(
                    value = form.motivo,
                    onValueChange = { onFormChange(form.copy(motivo = it)) },
                    placeholder = { Text("Ex: Mudança de casa, item não é mais utilizado...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
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

            val isSubmitting = state is CriarDoacaoState.Loading

            Button(
                onClick = onSubmit,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),
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
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Publicar Doação",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview
@Composable
private fun CriarDoacaoContentEmptyPreview() {
    DonationTheme {
        CriarDoacaoContent(
            state = CriarDoacaoState.Idle,
            form = DoacaoForm(),
            onFormChange = {},
            onNavigateBack = {},
            onSubmit = {},
        )
    }
}

@Preview
@Composable
private fun CriarDoacaoContentFilledPreview() {
    DonationTheme {
        CriarDoacaoContent(
            state = CriarDoacaoState.Idle,
            form =
                DoacaoForm(
                    nome = "Agasalho de Inverno",
                    quantidade = 2,
                    categoria = "Roupas",
                    eNovo = false,
                    descricao = "Agasalho tamanho M em excelente estado.",
                    motivo = "Não serve mais.",
                ),
            onFormChange = {},
            onNavigateBack = {},
            onSubmit = {},
        )
    }
}

@Preview
@Composable
private fun CriarDoacaoContentLoadingPreview() {
    DonationTheme(darkTheme = true) {
        CriarDoacaoContent(
            state = CriarDoacaoState.Loading,
            form =
                DoacaoForm(
                    nome = "Notebook Usado",
                    quantidade = 1,
                    categoria = "Eletrônicos",
                ),
            onFormChange = {},
            onNavigateBack = {},
            onSubmit = {},
        )
    }
}
