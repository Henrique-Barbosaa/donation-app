package io.solidar.donation.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.solidar.donation.ui.components.AppIcons

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel { AuthViewModel() },
) {
    val loginState by viewModel.loginState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Validação T3
    val isEmailError = email.isNotEmpty() && !isEmailValid(email)
    val isPasswordError = password.isNotEmpty() && !isPasswordValid(password)
    val isFormValid = isEmailValid(email) && isPasswordValid(password)

    LaunchedEffect(loginState) {
        if (loginState is AuthState.Success) {
            onLoginSuccess()
            viewModel.resetLoginState()
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), Color.Transparent),
                            radius = 600f,
                        ),
                    ),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                isError = isEmailError,
                supportingText = { if (isEmailError) Text("Insira um e-mail válido") },
                leadingIcon = { Icon(imageVector = AppIcons.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                isError = isPasswordError,
                supportingText = { if (isPasswordError) Text("A senha deve ter pelo menos 6 caracteres") },
                leadingIcon = { Icon(imageVector = AppIcons.Lock, contentDescription = null) },
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "Ocultar" else "Ver")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (isFormValid) viewModel.login(email, password) }),
                singleLine = true,
            )

            TextButton(
                onClick = { /* TODO: Forgot Password API */ },
                modifier = Modifier.align(Alignment.End).defaultMinSize(minHeight = 48.dp),
            ) {
                Text(
                    text = "Esqueci minha senha",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (loginState is AuthState.Error) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth().background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(12.dp),
                        ).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⚠️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = (loginState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = isFormValid && loginState !is AuthState.Loading,
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
                horizontalArrangement = Arrangement.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .clickable(role = Role.Button) { onNavigateToSignUp() }
                        .padding(8.dp),
            ) {
                Text("Ainda não faz parte? ", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Cadastre-se", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
