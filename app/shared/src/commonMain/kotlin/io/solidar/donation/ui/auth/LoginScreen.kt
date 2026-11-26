package io.solidar.donation.ui.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.solidar.donation.di.AppContainer
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.util.Validators

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel { AppContainer.default.createAuthViewModel() },
) {
    val uiState by viewModel.loginUiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.authState) {
        if (uiState.authState is AuthState.Success) {
            onLoginSuccess()
            viewModel.resetLoginState()
        }
    }

    LoginContent(
        email = uiState.email,
        onEmailChange = viewModel::onLoginEmailChange,
        password = uiState.password,
        onPasswordChange = viewModel::onLoginPasswordChange,
        passwordVisible = uiState.passwordVisible,
        onTogglePasswordVisibility = viewModel::toggleLoginPasswordVisibility,
        loginState = uiState.authState,
        onLogin = { viewModel.login() },
        onNavigateToSignUp = onNavigateToSignUp,
    )
}

@Composable
fun LoginContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    loginState: AuthState,
    onLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {},
) {
    val erroEmail = Validators.erroEmail(email)
    val erroSenha = Validators.erroSenha(password)
    val formValido = Validators.loginValido(email, password)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        // Efeito Glow de Fundo (Minimalista)
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0f),
                                ),
                            radius = 600f,
                        ),
                    ),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(32.dp),
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Bem-vindo de volta!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 34.sp,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Estamos felizes em ter você de novo por aqui.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("E-mail") },
                leadingIcon = {
                    Icon(imageVector = AppIcons.Email, contentDescription = null)
                },
                isError = erroEmail != null,
                supportingText =
                    if (erroEmail != null) {
                        { Text(erroEmail, color = MaterialTheme.colorScheme.error) }
                    } else {
                        null
                    },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    ),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Senha") },
                leadingIcon = {
                    Icon(imageVector = AppIcons.Lock, contentDescription = null)
                },
                isError = erroSenha != null,
                supportingText =
                    if (erroSenha != null) {
                        { Text(erroSenha, color = MaterialTheme.colorScheme.error) }
                    } else {
                        null
                    },
                trailingIcon = {
                    TextButton(
                        onClick = onTogglePasswordVisibility,
                        modifier =
                            Modifier
                                .heightIn(min = 48.dp)
                                .semantics {
                                    contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                                },
                    ) {
                        Text(if (passwordVisible) "Ocultar" else "Ver")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            if (formValido) {
                                onLogin()
                            }
                        },
                    ),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    ),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (loginState is AuthState.Error) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⚠️", fontSize = 18.sp, modifier = Modifier.clearAndSetSemantics {})
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = loginState.message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onLogin,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                enabled = formValido && loginState !is AuthState.Loading,
            ) {
                if (loginState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Entrando...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Ainda não faz parte? ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick = onNavigateToSignUp,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = "Cadastre-se",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoginContentEmptyPreview() {
    DonationTheme {
        LoginContent(
            email = "",
            onEmailChange = {},
            password = "",
            onPasswordChange = {},
            loginState = AuthState.Idle,
            onLogin = {},
            onNavigateToSignUp = {},
        )
    }
}

@Preview
@Composable
private fun LoginContentFilledPreview() {
    DonationTheme {
        LoginContent(
            email = "usuario@solidar.io",
            onEmailChange = {},
            password = "senhaSegura123",
            onPasswordChange = {},
            loginState = AuthState.Idle,
            onLogin = {},
            onNavigateToSignUp = {},
        )
    }
}

@Preview
@Composable
private fun LoginContentDarkPreview() {
    DonationTheme(darkTheme = true) {
        LoginContent(
            email = "usuario@solidar.io",
            onEmailChange = {},
            password = "senhaSegura123",
            onPasswordChange = {},
            loginState = AuthState.Loading,
            onLogin = {},
            onNavigateToSignUp = {},
        )
    }
}
