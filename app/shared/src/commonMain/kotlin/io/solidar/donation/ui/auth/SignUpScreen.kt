package io.solidar.donation.ui.auth

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.solidar.donation.di.AppContainer
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.util.Validators

private class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 11) text.text.substring(0..10) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if ((i == 2) || (i == 5)) out += "."
            if (i == 8) out += "-"
        }
        val offsetMapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 2) return offset
                    if (offset <= 5) return offset + 1
                    if (offset <= 8) return offset + 2
                    if (offset <= 11) return offset + 3
                    return 14
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 3) return offset
                    if (offset <= 7) return offset - 1
                    if (offset <= 11) return offset - 2
                    if (offset <= 14) return offset - 3
                    return 11
                }
            }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

private class CnpjVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 14) text.text.substring(0..13) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if ((i == 1) || (i == 4)) out += "."
            if (i == 7) out += "/"
            if (i == 11) out += "-"
        }
        val offsetMapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 1) return offset
                    if (offset <= 4) return offset + 1
                    if (offset <= 7) return offset + 2
                    if (offset <= 11) return offset + 3
                    if (offset <= 14) return offset + 4
                    return 18
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 2) return offset
                    if (offset <= 6) return offset - 1
                    if (offset <= 10) return offset - 2
                    if (offset <= 15) return offset - 3
                    if (offset <= 18) return offset - 4
                    return 14
                }
            }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel { AppContainer.default.createAuthViewModel() },
) {
    val uiState by viewModel.signUpUiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.authState) {
        if (uiState.authState is AuthState.Success) {
            onSignUpSuccess()
            viewModel.resetSignUpState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Criar Conta",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.currentStep == SignUpStep.CHOOSE_PROFILE) {
                                onNavigateBack()
                            } else {
                                viewModel.onSignUpStepChange(SignUpStep.CHOOSE_PROFILE)
                            }
                        },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Voltar",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Crossfade(targetState = uiState.currentStep) { step ->
                when (step) {
                    SignUpStep.CHOOSE_PROFILE ->
                        ProfileTypeSelection { selectedStep ->
                            viewModel.onSignUpStepChange(selectedStep)
                        }
                    SignUpStep.FORM_PF ->
                        SignUpForm(
                            isPf = true,
                            nome = uiState.nome,
                            onNomeChange = viewModel::onSignUpNomeChange,
                            email = uiState.email,
                            onEmailChange = viewModel::onSignUpEmailChange,
                            document = uiState.document,
                            onDocumentChange = { viewModel.onSignUpDocumentChange(it, 11) },
                            password = uiState.password,
                            onPasswordChange = viewModel::onSignUpPasswordChange,
                            passwordVisible = uiState.passwordVisible,
                            onPasswordVisibilityChange = viewModel::toggleSignUpPasswordVisibility,
                            signUpState = uiState.authState,
                            onSubmit = { viewModel.signUpPessoaFisica() },
                        )
                    SignUpStep.FORM_PJ ->
                        SignUpForm(
                            isPf = false,
                            nome = uiState.nome,
                            onNomeChange = viewModel::onSignUpNomeChange,
                            email = uiState.email,
                            onEmailChange = viewModel::onSignUpEmailChange,
                            document = uiState.document,
                            onDocumentChange = { viewModel.onSignUpDocumentChange(it, 14) },
                            password = uiState.password,
                            onPasswordChange = viewModel::onSignUpPasswordChange,
                            passwordVisible = uiState.passwordVisible,
                            onPasswordVisibilityChange = viewModel::toggleSignUpPasswordVisibility,
                            signUpState = uiState.authState,
                            onSubmit = { viewModel.signUpInstituicao() },
                        )
                }
            }
        }
    }
}

@Composable
fun ProfileTypeSelection(onSelectProfile: (SignUpStep) -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Como você deseja usar a plataforma?",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier =
                Modifier
                    .padding(bottom = 32.dp)
                    .semantics { heading() },
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { onSelectProfile(SignUpStep.FORM_PF) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = AppIcons.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Pessoa Física",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Quero doar itens, receber doações ou ser voluntário",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { onSelectProfile(SignUpStep.FORM_PJ) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = AppIcons.Home,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Instituição / ONG",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Represento uma ONG, quero criar campanhas e buscar voluntários",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SignUpForm(
    isPf: Boolean,
    nome: String,
    onNomeChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    document: String,
    onDocumentChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    signUpState: AuthState,
    onSubmit: () -> Unit,
) {
    val isValid = Validators.signUpValido(isPf, nome, email, document, password)

    val erroNome = if (nome.isNotBlank() && nome.trim().length < 3) "O nome deve ter no mínimo 3 caracteres." else null
    val erroEmail = Validators.erroEmail(email)
    val erroDoc =
        when {
            document.isBlank() -> null
            isPf && !Validators.cpfValido(document) -> "O CPF deve ter 11 dígitos."
            !isPf && !Validators.cnpjValido(document) -> "O CNPJ deve ter 14 dígitos."
            else -> null
        }
    val erroSenha = Validators.erroSenha(password)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isPf) "Cadastro de Pessoa Física" else "Cadastro de Organização",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .semantics { heading() },
        )

        OutlinedTextField(
            value = nome,
            onValueChange = onNomeChange,
            label = { Text(if (isPf) "Nome completo" else "Nome da Organização") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            isError = erroNome != null,
            supportingText = {
                erroNome?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            isError = erroEmail != null,
            supportingText = {
                erroEmail?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = document,
            onValueChange = onDocumentChange,
            label = { Text(if (isPf) "CPF (apenas dígitos)" else "CNPJ (apenas dígitos)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (isPf) CpfVisualTransformation() else CnpjVisualTransformation(),
            isError = erroDoc != null,
            supportingText = {
                erroDoc?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Senha") },
            isError = erroSenha != null,
            supportingText = {
                erroSenha?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            trailingIcon = {
                TextButton(
                    onClick = onPasswordVisibilityChange,
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
                        if (isValid) onSubmit()
                    },
                ),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (signUpState is AuthState.Error) {
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
                    text = signUpState.message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        Button(
            onClick = onSubmit,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = isValid && signUpState !is AuthState.Loading,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
        ) {
            if (signUpState is AuthState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Cadastrando...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else {
                Text("Cadastrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview
@Composable
private fun ProfileTypeSelectionPreview() {
    DonationTheme {
        ProfileTypeSelection(onSelectProfile = {})
    }
}

@Preview
@Composable
private fun SignUpFormPfPreview() {
    DonationTheme {
        SignUpForm(
            isPf = true,
            nome = "Maria Oliveira",
            onNomeChange = {},
            email = "maria@exemplo.com",
            onEmailChange = {},
            document = "12345678901",
            onDocumentChange = {},
            password = "senhaSegura123",
            onPasswordChange = {},
            passwordVisible = false,
            onPasswordVisibilityChange = {},
            signUpState = AuthState.Idle,
            onSubmit = {},
        )
    }
}

@Preview
@Composable
private fun SignUpFormDarkPreview() {
    DonationTheme(darkTheme = true) {
        SignUpForm(
            isPf = false,
            nome = "ONG Esperança",
            onNomeChange = {},
            email = "contato@ong.org",
            onEmailChange = {},
            document = "12345678000199",
            onDocumentChange = {},
            password = "senha",
            onPasswordChange = {},
            passwordVisible = false,
            onPasswordVisibilityChange = {},
            signUpState = AuthState.Idle,
            onSubmit = {},
        )
    }
}
