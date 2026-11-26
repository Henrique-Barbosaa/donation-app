package io.solidar.donation.ui.profile

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.util.PerfilForm
import io.solidar.donation.util.validarPerfil

private class CepVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 4) out += "-"
        }
        val offsetMapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 4) return offset
                    if (offset <= 8) return offset + 1
                    return 9
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 5) return offset
                    if (offset <= 9) return offset - 1
                    return 8
                }
            }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

/**
 * Tela de edição dos dados cadastrais e endereço do usuário autenticado (`PUT /api/usuarios/perfil`).
 * Lê o estado do [PerfilViewModel] com [collectAsStateWithLifecycle], sem guardar estado em `remember`.
 */
@Composable
fun EditarPerfilScreen(
    viewModel: PerfilViewModel,
    onNavigateBack: () -> Unit,
    onProfileUpdated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.submissaoPerfil) {
        if (uiState.submissaoPerfil is SubmissaoPerfilState.Success) {
            viewModel.resetSubmissaoPerfil()
            onProfileUpdated()
        }
    }

    EditarPerfilContent(
        form = uiState.formPerfil,
        onFormChange = viewModel::atualizarFormPerfil,
        submissaoState = uiState.submissaoPerfil,
        isInstituicao = uiState.perfilAtual?.isInstituicao == true,
        onNavigateBack = {
            viewModel.resetSubmissaoPerfil()
            onNavigateBack()
        },
        onSubmit = viewModel::salvarPerfil,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilContent(
    form: PerfilForm,
    onFormChange: (PerfilForm) -> Unit,
    submissaoState: SubmissaoPerfilState,
    onNavigateBack: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isInstituicao: Boolean = false,
) {
    val erros = validarPerfil(form)
    val isSubmitting = submissaoState is SubmissaoPerfilState.Loading

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar Perfil",
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
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (submissaoState is SubmissaoPerfilState.Error) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = submissaoState.mensagem,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                Text(
                    text = "Dados de identificação",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() },
                )

                val showNomeError = erros.nome != null
                OutlinedTextField(
                    value = form.nome,
                    onValueChange = { onFormChange(form.copy(nome = it)) },
                    label = {
                        Text(if (isInstituicao) "Nome da Organização *" else "Nome completo *")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Endereço para doações e coletas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() },
                )

                val showCepError = erros.cep != null
                OutlinedTextField(
                    value = form.cep,
                    onValueChange = { onFormChange(form.copy(cep = it)) },
                    label = { Text("CEP (apenas dígitos)") },
                    placeholder = { Text("00000-000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    visualTransformation = CepVisualTransformation(),
                    isError = showCepError,
                    supportingText = {
                        if (showCepError) {
                            Text(
                                text = erros.cep,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
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

                val showLogradouroError = erros.logradouro != null
                OutlinedTextField(
                    value = form.logradouro,
                    onValueChange = { onFormChange(form.copy(logradouro = it)) },
                    label = { Text("Logradouro (Rua, Avenida)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = showLogradouroError,
                    supportingText = {
                        if (showLogradouroError) {
                            Text(
                                text = erros.logradouro,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val showNumeroError = erros.numero != null
                    OutlinedTextField(
                        value = form.numero,
                        onValueChange = { onFormChange(form.copy(numero = it)) },
                        label = { Text("Número") },
                        modifier = Modifier.weight(0.4f),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        isError = showNumeroError,
                        supportingText = {
                            if (showNumeroError) {
                                Text(
                                    text = erros.numero,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                imeAction = ImeAction.Next,
                            ),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                            ),
                    )

                    val showBairroError = erros.bairro != null
                    OutlinedTextField(
                        value = form.bairro,
                        onValueChange = { onFormChange(form.copy(bairro = it)) },
                        label = { Text("Bairro") },
                        modifier = Modifier.weight(0.6f),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        isError = showBairroError,
                        supportingText = {
                            if (showBairroError) {
                                Text(
                                    text = erros.bairro,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done,
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onDone = {
                                    if (erros.valido && !isSubmitting) {
                                        onSubmit()
                                    }
                                },
                            ),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                            ),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSubmit,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp),
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
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Salvando...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        Text(
                            text = "Salvar alterações",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
