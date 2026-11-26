package io.solidar.donation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.domain.model.AlterarSenhaInput
import io.solidar.donation.domain.model.AtualizarPerfilInput
import io.solidar.donation.domain.model.PerfilUsuario
import io.solidar.donation.domain.repository.PerfilRepository
import io.solidar.donation.ui.state.TelaEstado
import io.solidar.donation.ui.state.mapearMensagemDeErro
import io.solidar.donation.util.AlterarSenhaForm
import io.solidar.donation.util.PerfilForm
import io.solidar.donation.util.validarAlterarSenha
import io.solidar.donation.util.validarPerfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SubmissaoPerfilState {
    data object Idle : SubmissaoPerfilState()

    data object Loading : SubmissaoPerfilState()

    data class Success(val mensagem: String) : SubmissaoPerfilState()

    data class Error(val mensagem: String) : SubmissaoPerfilState()
}

data class PerfilUiState(
    val estadoPerfil: TelaEstado<PerfilUsuario> = TelaEstado.Carregando,
    val formPerfil: PerfilForm = PerfilForm(),
    val submissaoPerfil: SubmissaoPerfilState = SubmissaoPerfilState.Idle,
    val formSenha: AlterarSenhaForm = AlterarSenhaForm(),
    val senhaAtualVisivel: Boolean = false,
    val novaSenhaVisivel: Boolean = false,
    val confirmarSenhaVisivel: Boolean = false,
    val submissaoSenha: SubmissaoPerfilState = SubmissaoPerfilState.Idle,
    val mensagemFeedback: String? = null,
) {
    val perfilAtual: PerfilUsuario?
        get() = (estadoPerfil as? TelaEstado.Sucesso)?.dados
}

/**
 * ViewModel multiplataforma responsável pelo carregamento do perfil do usuário,
 * edição de dados cadastrais/endereço e alteração de senha.
 *
 * Recebe [PerfilRepository] pelo construtor (injeção de dependência).
 */
class PerfilViewModel(
    private val repository: PerfilRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        carregarPerfil()
    }

    fun carregarPerfil() {
        viewModelScope.launch {
            _uiState.update { it.copy(estadoPerfil = TelaEstado.Carregando) }
            val result = repository.carregarPerfil()
            result.fold(
                onSuccess = { resultado ->
                    val perfil = resultado.dados
                    if (perfil.nome.isBlank() && perfil.email.isBlank()) {
                        _uiState.update { it.copy(estadoPerfil = TelaEstado.Vazio) }
                    } else {
                        _uiState.update {
                            it.copy(
                                estadoPerfil =
                                    TelaEstado.Sucesso(
                                        dados = perfil,
                                        desatualizado = resultado.desatualizado,
                                    ),
                                formPerfil =
                                    PerfilForm(
                                        nome = perfil.nome,
                                        cep = perfil.cep.filter { ch -> ch.isDigit() }.take(8),
                                        logradouro = perfil.logradouro,
                                        numero = perfil.numero,
                                        bairro = perfil.bairro,
                                    ),
                            )
                        }
                    }
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            estadoPerfil = TelaEstado.Erro(mapearMensagemDeErro(erro)),
                        )
                    }
                },
            )
        }
    }

    fun prepararEdicaoPerfil() {
        val perfil = _uiState.value.perfilAtual
        _uiState.update {
            it.copy(
                submissaoPerfil = SubmissaoPerfilState.Idle,
                mensagemFeedback = null,
                formPerfil =
                    if (perfil != null) {
                        PerfilForm(
                            nome = perfil.nome,
                            cep = perfil.cep.filter { ch -> ch.isDigit() }.take(8),
                            logradouro = perfil.logradouro,
                            numero = perfil.numero,
                            bairro = perfil.bairro,
                        )
                    } else {
                        it.formPerfil
                    },
            )
        }
    }

    fun atualizarFormPerfil(novoForm: PerfilForm) {
        val cepLimpo = novoForm.cep.filter { it.isDigit() }.take(8)
        _uiState.update {
            it.copy(
                formPerfil = novoForm.copy(cep = cepLimpo),
            )
        }
    }

    fun salvarPerfil() {
        val form = _uiState.value.formPerfil
        val erros = validarPerfil(form)
        if (!erros.valido) {
            val msg =
                erros.nome
                    ?: erros.cep
                    ?: erros.logradouro
                    ?: erros.numero
                    ?: erros.bairro
                    ?: "Verifique os campos preenchidos."
            _uiState.update {
                it.copy(submissaoPerfil = SubmissaoPerfilState.Error(msg))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(submissaoPerfil = SubmissaoPerfilState.Loading) }
            val input =
                AtualizarPerfilInput(
                    nome = form.nome.trim(),
                    logradouro = form.logradouro.trim(),
                    numero = form.numero.trim(),
                    bairro = form.bairro.trim(),
                    cep = form.cep.filter { it.isDigit() },
                )
            val result = repository.atualizarPerfil(input)
            result.fold(
                onSuccess = { perfilAtualizado ->
                    _uiState.update {
                        it.copy(
                            estadoPerfil =
                                TelaEstado.Sucesso(
                                    dados = perfilAtualizado,
                                    desatualizado = false,
                                ),
                            submissaoPerfil =
                                SubmissaoPerfilState.Success("Dados do perfil atualizados com sucesso!"),
                            mensagemFeedback = "Dados do perfil atualizados com sucesso!",
                        )
                    }
                },
                onFailure = { erro ->
                    // Mantém os dados preenchidos no formulário na tela e exibe o aviso de erro
                    _uiState.update {
                        it.copy(
                            submissaoPerfil = SubmissaoPerfilState.Error(mapearMensagemDeErro(erro)),
                        )
                    }
                },
            )
        }
    }

    fun resetSubmissaoPerfil() {
        _uiState.update { it.copy(submissaoPerfil = SubmissaoPerfilState.Idle) }
    }

    fun prepararAlteracaoSenha() {
        _uiState.update {
            it.copy(
                formSenha = AlterarSenhaForm(),
                senhaAtualVisivel = false,
                novaSenhaVisivel = false,
                confirmarSenhaVisivel = false,
                submissaoSenha = SubmissaoPerfilState.Idle,
                mensagemFeedback = null,
            )
        }
    }

    fun atualizarFormSenha(novoForm: AlterarSenhaForm) {
        _uiState.update { it.copy(formSenha = novoForm) }
    }

    fun alternarVisibilidadeSenhaAtual() {
        _uiState.update { it.copy(senhaAtualVisivel = !it.senhaAtualVisivel) }
    }

    fun alternarVisibilidadeNovaSenha() {
        _uiState.update { it.copy(novaSenhaVisivel = !it.novaSenhaVisivel) }
    }

    fun alternarVisibilidadeConfirmarSenha() {
        _uiState.update { it.copy(confirmarSenhaVisivel = !it.confirmarSenhaVisivel) }
    }

    fun salvarNovaSenha() {
        val form = _uiState.value.formSenha
        val erros = validarAlterarSenha(form)
        if (!erros.valido) {
            val msg =
                erros.senhaAtual
                    ?: erros.novaSenha
                    ?: erros.confirmarNovaSenha
                    ?: "Verifique os campos de senha."
            _uiState.update {
                it.copy(submissaoSenha = SubmissaoPerfilState.Error(msg))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(submissaoSenha = SubmissaoPerfilState.Loading) }
            val result =
                repository.alterarSenha(
                    AlterarSenhaInput(
                        senhaAtual = form.senhaAtual,
                        novaSenha = form.novaSenha,
                    ),
                )
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            formSenha = AlterarSenhaForm(),
                            submissaoSenha =
                                SubmissaoPerfilState.Success("Senha alterada com sucesso!"),
                            mensagemFeedback = "Senha alterada com sucesso!",
                        )
                    }
                },
                onFailure = { erro ->
                    // Mantém os dados na tela e mostra o erro
                    _uiState.update {
                        it.copy(
                            submissaoSenha = SubmissaoPerfilState.Error(mapearMensagemDeErro(erro)),
                        )
                    }
                },
            )
        }
    }

    fun resetSubmissaoSenha() {
        _uiState.update { it.copy(submissaoSenha = SubmissaoPerfilState.Idle) }
    }

    fun limparMensagemFeedback() {
        _uiState.update { it.copy(mensagemFeedback = null) }
    }
}
