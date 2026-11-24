package io.solidar.donation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.domain.repository.AuthRepository
import io.solidar.donation.ui.state.mapearMensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SignUpStep {
    CHOOSE_PROFILE,
    FORM_PF,
    FORM_PJ,
}

sealed class AuthState {
    data object Idle : AuthState()

    data object Loading : AuthState()

    data class Success(val message: String) : AuthState()

    data class Error(val message: String) : AuthState()
}

data class LoginFormUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val authState: AuthState = AuthState.Idle,
)

data class SignUpFormUiState(
    val currentStep: SignUpStep = SignUpStep.CHOOSE_PROFILE,
    val nome: String = "",
    val email: String = "",
    val document: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val authState: AuthState = AuthState.Idle,
)

/**
 * ViewModel multiplataforma de autenticação (Login e Cadastro).
 * Recebe [AuthRepository] pelo construtor (injeção de dependência).
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _loginUiState = MutableStateFlow(LoginFormUiState())
    val loginUiState: StateFlow<LoginFormUiState> = _loginUiState.asStateFlow()

    private val _signUpUiState = MutableStateFlow(SignUpFormUiState())
    val signUpUiState: StateFlow<SignUpFormUiState> = _signUpUiState.asStateFlow()

    fun onLoginEmailChange(email: String) {
        _loginUiState.update { it.copy(email = email) }
    }

    fun onLoginPasswordChange(password: String) {
        _loginUiState.update { it.copy(password = password) }
    }

    fun toggleLoginPasswordVisibility() {
        _loginUiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onSignUpStepChange(step: SignUpStep) {
        _signUpUiState.update {
            it.copy(
                currentStep = step,
                document = "",
                authState = AuthState.Idle,
            )
        }
    }

    fun onSignUpNomeChange(nome: String) {
        _signUpUiState.update { it.copy(nome = nome) }
    }

    fun onSignUpEmailChange(email: String) {
        _signUpUiState.update { it.copy(email = email) }
    }

    fun onSignUpDocumentChange(
        rawDocument: String,
        maxDigits: Int,
    ) {
        val cleaned = rawDocument.filter { it.isDigit() }.take(maxDigits)
        _signUpUiState.update { it.copy(document = cleaned) }
    }

    fun onSignUpPasswordChange(password: String) {
        _signUpUiState.update { it.copy(password = password) }
    }

    fun toggleSignUpPasswordVisibility() {
        _signUpUiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun login(
        email: String = _loginUiState.value.email,
        senha: String = _loginUiState.value.password,
    ) {
        if (email.isBlank() || senha.isBlank()) {
            _loginUiState.update {
                it.copy(authState = AuthState.Error("Por favor, preencha o e-mail e a senha."))
            }
            return
        }

        viewModelScope.launch {
            _loginUiState.update { it.copy(authState = AuthState.Loading) }
            val result = authRepository.login(email, senha)
            result.fold(
                onSuccess = { session ->
                    _loginUiState.update {
                        it.copy(authState = AuthState.Success("Bem-vindo de volta, ${session.nome}!"))
                    }
                },
                onFailure = { error ->
                    _loginUiState.update {
                        it.copy(authState = AuthState.Error(mapearMensagemDeErro(error)))
                    }
                },
            )
        }
    }

    fun signUpPessoaFisica(
        nome: String = _signUpUiState.value.nome,
        email: String = _signUpUiState.value.email,
        senha: String = _signUpUiState.value.password,
        cpf: String = _signUpUiState.value.document,
    ) {
        if (nome.isBlank() || email.isBlank() || senha.isBlank() || cpf.isBlank()) {
            _signUpUiState.update {
                it.copy(authState = AuthState.Error("Por favor, preencha todos os campos obrigatórios."))
            }
            return
        }

        viewModelScope.launch {
            _signUpUiState.update { it.copy(authState = AuthState.Loading) }
            val result = authRepository.registerPessoaFisica(nome, email, senha, cpf)
            result.fold(
                onSuccess = { session ->
                    _signUpUiState.update {
                        it.copy(authState = AuthState.Success("Cadastro realizado com sucesso! Bem-vindo, ${session.nome}."))
                    }
                },
                onFailure = { error ->
                    _signUpUiState.update {
                        it.copy(authState = AuthState.Error(mapearMensagemDeErro(error)))
                    }
                },
            )
        }
    }

    fun signUpInstituicao(
        nome: String = _signUpUiState.value.nome,
        email: String = _signUpUiState.value.email,
        senha: String = _signUpUiState.value.password,
        cnpj: String = _signUpUiState.value.document,
    ) {
        if (nome.isBlank() || email.isBlank() || senha.isBlank() || cnpj.isBlank()) {
            _signUpUiState.update {
                it.copy(authState = AuthState.Error("Por favor, preencha todos os campos obrigatórios."))
            }
            return
        }

        viewModelScope.launch {
            _signUpUiState.update { it.copy(authState = AuthState.Loading) }
            val result = authRepository.registerInstituicao(nome, email, senha, cnpj)
            result.fold(
                onSuccess = { session ->
                    _signUpUiState.update {
                        it.copy(authState = AuthState.Success("Instituição cadastrada com sucesso! Bem-vindo, ${session.nome}."))
                    }
                },
                onFailure = { error ->
                    _signUpUiState.update {
                        it.copy(authState = AuthState.Error(mapearMensagemDeErro(error)))
                    }
                },
            )
        }
    }

    fun resetLoginState() {
        _loginUiState.update { it.copy(authState = AuthState.Idle) }
    }

    fun resetSignUpState() {
        _signUpUiState.update { it.copy(authState = AuthState.Idle) }
    }

    fun logout() {
        authRepository.logout()
        _loginUiState.value = LoginFormUiState()
        _signUpUiState.value = SignUpFormUiState()
    }
}
