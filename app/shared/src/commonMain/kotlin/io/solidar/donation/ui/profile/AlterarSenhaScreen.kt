package io.solidar.donation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.util.AlterarSenhaForm
import io.solidar.donation.util.validarAlterarSenha

/**
 * Tela de alteração de senha do usuário autenticado (`PUT /api/usuarios/senha`).
 * Lê o estado de [PerfilViewModel] com [collectAsStateWithLifecycle], sem guardar estado em `remember`.
 */
@Composable
fun AlterarSenhaScreen(
    viewModel: PerfilViewModel,
    onNavigateBack: () -> Unit,
    onPasswordChanged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.submissaoSenha) {
        if (uiState.submissaoSenha is SubmissaoPerfilState.Success) {
            viewModel.resetSubmissaoSenha()
            onPasswordChanged()
        }
    }

    AlterarSenhaContent(
        form = uiState.formSenha,
        onFormChange = viewModel::atualizarFormSenha,
        senhaAtualVisivel = uiState.senhaAtualVisivel,
        onToggleSenhaAtualVisivel = viewModel::alternarVisibilidadeSenhaAtual,
        novaSenhaVisivel = uiState.novaSenhaVisivel,
        onToggleNovaSenhaVisivel = viewModel::alternarVisibilidadeNovaSenha,
        confirmarSenhaVisivel = uiState.confirmarSenhaVisivel,
        onToggleConfirmarSenhaVisivel = viewModel::alternarVisibilidadeConfirmarSenha,
        submissaoState = uiState.submissaoSenha,
        onNavigateBack = {
            viewModel.resetSubmissaoSenha()
            onNavigateBack()
        },
        onSubmit = viewModel::salvarNovaSenha,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlterarSenhaContent(
    form: AlterarSenhaForm,
    onFormChange: (AlterarSenhaForm) -> Unit,
    submissaoState: SubmissaoPerfilState,
    onNavigateBack: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    senhaAtualVisivel: Boolean = false,
    onToggleSenhaAtualVisivel: () -> Unit = {},
    novaSenhaVisivel: Boolean = false,
    onToggleNovaSenhaVisivel: () -> Unit = {},
    confirmarSenhaVisivel: Boolean = false,
    onToggleConfirmarSenhaVisivel: () -> Unit = {},
) {
    val erros = validarAlterarSenha(form)
    val isSubmitting = submissaoState is SubmissaoPerfilState.Loading

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Alterar Senha",
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
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Defina uma nova senha de acesso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() },
                )

                Text(
                    text = "Para sua segurança, informe a senha atual e escolha uma nova senha com pelo menos 6 caracteres.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

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

                val showSenhaAtualError = erros.senhaAtual != null && form.senhaAtual.isNotEmpty()
                OutlinedTextField(
                    value = form.senhaAtual,
                    onValueChange = { onFormChange(form.copy(senhaAtual = it)) },
                    label = { Text("Senha atual *") },
                    leadingIcon = {
                        Icon(imageVector = AppIcons.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = onToggleSenhaAtualVisivel,
                            modifier =
                                Modifier
                                    .heightIn(min = 48.dp)
                                    .semantics {
                                        contentDescription =
                                            if (senhaAtualVisivel) {
                                                "Ocultar senha atual"
                                            } else {
                                                "Mostrar senha atual"
                                            }
                                    },
                        ) {
                            Text(if (senhaAtualVisivel) "Ocultar" else "Ver")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = showSenhaAtualError,
                    supportingText = {
                        if (showSenhaAtualError) {
                            Text(
                                text = erros.senhaAtual,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    },
                    visualTransformation =
                        if (senhaAtualVisivel) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                        ),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                )

                val showNovaSenhaError = erros.novaSenha != null && form.novaSenha.isNotEmpty()
                OutlinedTextField(
                    value = form.novaSenha,
                    onValueChange = { onFormChange(form.copy(novaSenha = it)) },
                    label = { Text("Nova senha *") },
                    leadingIcon = {
                        Icon(imageVector = AppIcons.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = onToggleNovaSenhaVisivel,
                            modifier =
                                Modifier
                                    .heightIn(min = 48.dp)
                                    .semantics {
                                        contentDescription =
                                            if (novaSenhaVisivel) {
                                                "Ocultar nova senha"
                                            } else {
                                                "Mostrar nova senha"
                                            }
                                    },
                        ) {
                            Text(if (novaSenhaVisivel) "Ocultar" else "Ver")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = showNovaSenhaError,
                    supportingText = {
                        if (showNovaSenhaError) {
                            Text(
                                text = erros.novaSenha,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    },
                    visualTransformation =
                        if (novaSenhaVisivel) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                        ),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                )

                val showConfirmarError =
                    erros.confirmarNovaSenha != null && form.confirmarNovaSenha.isNotEmpty()
                OutlinedTextField(
                    value = form.confirmarNovaSenha,
                    onValueChange = { onFormChange(form.copy(confirmarNovaSenha = it)) },
                    label = { Text("Confirmar nova senha *") },
                    leadingIcon = {
                        Icon(imageVector = AppIcons.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = onToggleConfirmarSenhaVisivel,
                            modifier =
                                Modifier
                                    .heightIn(min = 48.dp)
                                    .semantics {
                                        contentDescription =
                                            if (confirmarSenhaVisivel) {
                                                "Ocultar confirmação de senha"
                                            } else {
                                                "Mostrar confirmação de senha"
                                            }
                                    },
                        ) {
                            Text(if (confirmarSenhaVisivel) "Ocultar" else "Ver")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = showConfirmarError,
                    supportingText = {
                        if (showConfirmarError) {
                            Text(
                                text = erros.confirmarNovaSenha,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    },
                    visualTransformation =
                        if (confirmarSenhaVisivel) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
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
                            text = "Atualizando...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        Text(
                            text = "Atualizar senha",
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
