package io.solidar.donation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.data.repository.AuthRepositoryImpl
import io.solidar.donation.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()

    object Loading : AuthState()

    data class Success(val message: String) : AuthState()

    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
) : ViewModel() {
    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow<AuthState>(AuthState.Idle)
    val signUpState: StateFlow<AuthState> = _signUpState.asStateFlow()

    fun login(
        email: String,
        senha: String,
    ) {
        if (email.isBlank() || senha.isBlank()) {
            _loginState.value = AuthState.Error("Por favor, preencha o e-mail e a senha.")
            return
        }

        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            val result = authRepository.login(email, senha)
            result.fold(
                onSuccess = { session ->
                    _loginState.value = AuthState.Success("Bem-vindo de volta, ${session.nome}!")
                },
                onFailure = { error ->
                    _loginState.value =
                        AuthState.Error(
                            error.message ?: "Não foi possível conectar ao servidor. Verifique sua conexão e se a API está ativa.",
                        )
                },
            )
        }
    }

    fun signUpPessoaFisica(
        nome: String,
        email: String,
        senha: String,
        cpf: String,
    ) {
        if (nome.isBlank() || email.isBlank() || senha.isBlank() || cpf.isBlank()) {
            _signUpState.value = AuthState.Error("Por favor, preencha todos os campos obrigatórios.")
            return
        }

        viewModelScope.launch {
            _signUpState.value = AuthState.Loading
            val result = authRepository.registerPessoaFisica(nome, email, senha, cpf)
            result.fold(
                onSuccess = { session ->
                    _signUpState.value = AuthState.Success("Cadastro realizado com sucesso! Bem-vindo, ${session.nome}.")
                },
                onFailure = { error ->
                    _signUpState.value =
                        AuthState.Error(
                            error.message ?: "Ocorreu um erro ao tentar cadastrar. Tente novamente mais tarde.",
                        )
                },
            )
        }
    }

    fun signUpInstituicao(
        nome: String,
        email: String,
        senha: String,
        cnpj: String,
    ) {
        if (nome.isBlank() || email.isBlank() || senha.isBlank() || cnpj.isBlank()) {
            _signUpState.value = AuthState.Error("Por favor, preencha todos os campos obrigatórios.")
            return
        }

        viewModelScope.launch {
            _signUpState.value = AuthState.Loading
            val result = authRepository.registerInstituicao(nome, email, senha, cnpj)
            result.fold(
                onSuccess = { session ->
                    _signUpState.value = AuthState.Success("Instituição cadastrada com sucesso! Bem-vindo, ${session.nome}.")
                },
                onFailure = { error ->
                    _signUpState.value =
                        AuthState.Error(
                            error.message ?: "Ocorreu um erro ao tentar cadastrar. Tente novamente mais tarde.",
                        )
                },
            )
        }
    }

    fun resetLoginState() {
        _loginState.value = AuthState.Idle
    }

    fun resetSignUpState() {
        _signUpState.value = AuthState.Idle
    }
}
