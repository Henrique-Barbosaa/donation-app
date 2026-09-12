package org.donation.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class AuthViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow<AuthState>(AuthState.Idle)
    val signUpState: StateFlow<AuthState> = _signUpState.asStateFlow()

    fun login(email: String, senha: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            try {
                // TODO: Chamada real ao backend: POST /api/auth/login
                kotlinx.coroutines.delay(1000)
                
                if (email.isBlank() || senha.isBlank()) {
                    _loginState.value = AuthState.Error("Por favor, preencha o e-mail e a senha.")
                    return@launch
                }
                
                // Simulação de erro de rede se o usuário digitar "erro"
                if (email.contains("erro", ignoreCase = true)) {
                    throw Exception("Servidor offline")
                }

                _loginState.value = AuthState.Success("Login realizado com sucesso!")
            } catch (e: Exception) {
                _loginState.value = AuthState.Error("Não foi possível conectar ao servidor. Verifique sua internet e tente novamente.")
            }
        }
    }

    fun signUpPessoaFisica(nome: String, email: String, senha: String, cpf: String) {
        viewModelScope.launch {
            _signUpState.value = AuthState.Loading
            try {
                // TODO: Chamada real ao backend: POST /api/auth/registro/pessoa-fisica
                kotlinx.coroutines.delay(1000)
                
                if (nome.isBlank() || email.isBlank() || senha.isBlank() || cpf.isBlank()) {
                    _signUpState.value = AuthState.Error("Por favor, preencha todos os campos corretamente.")
                    return@launch
                }
                
                if (email.contains("erro", ignoreCase = true)) {
                    throw Exception("Servidor offline")
                }

                _signUpState.value = AuthState.Success("Cadastro realizado com sucesso!")
            } catch (e: Exception) {
                _signUpState.value = AuthState.Error("Ocorreu um erro ao tentar cadastrar. Tente novamente mais tarde.")
            }
        }
    }

    fun signUpInstituicao(nome: String, email: String, senha: String, cnpj: String) {
        viewModelScope.launch {
            _signUpState.value = AuthState.Loading
            try {
                // TODO: Chamada real ao backend: POST /api/auth/registro/instituicao
                kotlinx.coroutines.delay(1000)
                
                if (nome.isBlank() || email.isBlank() || senha.isBlank() || cnpj.isBlank()) {
                    _signUpState.value = AuthState.Error("Por favor, preencha todos os campos corretamente.")
                    return@launch
                }

                if (email.contains("erro", ignoreCase = true)) {
                    throw Exception("Servidor offline")
                }

                _signUpState.value = AuthState.Success("Instituição cadastrada com sucesso!")
            } catch (e: Exception) {
                _signUpState.value = AuthState.Error("Serviço temporariamente indisponível. Tente novamente mais tarde.")
            }
        }
    }
    
    fun resetLoginState() {
        _loginState.value = AuthState.Idle
    }
    
    fun resetSignUpState() {
        _signUpState.value = AuthState.Idle
    }
}