package io.solidar.donation.ui.donation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.domain.model.EfetivarDoacaoInput
import io.solidar.donation.domain.repository.DoacaoRepository
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.Interessado
import io.solidar.donation.ui.state.TelaEstado
import io.solidar.donation.ui.state.mapearMensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class EfetivarEnvioState {
    data object Idle : EfetivarEnvioState()

    data object Loading : EfetivarEnvioState()

    data class Success(val doacao: Doacao) : EfetivarEnvioState()

    data class Error(val message: String) : EfetivarEnvioState()
}

data class EfetivarDoacaoUiState(
    val estadoInteressados: TelaEstado<List<Interessado>> = TelaEstado.Carregando,
    val interessadoSelecionadoId: Int? = null,
    val nomeLocal: String = "",
    val logradouro: String = "",
    val bairro: String = "",
    val envioState: EfetivarEnvioState = EfetivarEnvioState.Idle,
) {
    val podeConfirmar: Boolean
        get() =
            interessadoSelecionadoId != null &&
                nomeLocal.trim().length >= 3 &&
                envioState !is EfetivarEnvioState.Loading
}

/**
 * ViewModel multiplataforma da efetivação de doação (issue #10):
 * lista os interessados de uma doação pendente e efetiva a entrega para o selecionado.
 */
class EfetivarDoacaoViewModel(
    private val repository: DoacaoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EfetivarDoacaoUiState())
    val uiState: StateFlow<EfetivarDoacaoUiState> = _uiState.asStateFlow()

    fun carregarInteressados(doacaoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(estadoInteressados = TelaEstado.Carregando) }
            val result = repository.listarInteressados(doacaoId)
            result.fold(
                onSuccess = { lista ->
                    val novoEstado =
                        if (lista.isEmpty()) {
                            TelaEstado.Vazio
                        } else {
                            TelaEstado.Sucesso(dados = lista)
                        }
                    _uiState.update { atual ->
                        atual.copy(
                            estadoInteressados = novoEstado,
                            interessadoSelecionadoId =
                                atual.interessadoSelecionadoId
                                    ?.takeIf { idSel -> lista.any { it.id == idSel } }
                                    ?: lista.firstOrNull()?.id,
                        )
                    }
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(estadoInteressados = TelaEstado.Erro(mapearMensagemDeErro(erro)))
                    }
                },
            )
        }
    }

    fun selecionarInteressado(id: Int) {
        _uiState.update {
            it.copy(
                interessadoSelecionadoId = id,
                envioState = if (it.envioState is EfetivarEnvioState.Error) EfetivarEnvioState.Idle else it.envioState,
            )
        }
    }

    fun onNomeLocalChange(valor: String) {
        _uiState.update {
            it.copy(
                nomeLocal = valor,
                envioState = if (it.envioState is EfetivarEnvioState.Error) EfetivarEnvioState.Idle else it.envioState,
            )
        }
    }

    fun onLogradouroChange(valor: String) {
        _uiState.update { it.copy(logradouro = valor) }
    }

    fun onBairroChange(valor: String) {
        _uiState.update { it.copy(bairro = valor) }
    }

    fun resetEnvio() {
        _uiState.update { it.copy(envioState = EfetivarEnvioState.Idle) }
    }

    fun confirmarEfetivacao(doacaoId: String) {
        val atual = _uiState.value
        val donatarioId = atual.interessadoSelecionadoId
        if (donatarioId == null) {
            _uiState.update {
                it.copy(envioState = EfetivarEnvioState.Error("Selecione quem vai receber a doação."))
            }
            return
        }
        if (atual.nomeLocal.trim().length < 3) {
            _uiState.update {
                it.copy(envioState = EfetivarEnvioState.Error("Informe o nome do local de entrega (mínimo 3 caracteres)."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(envioState = EfetivarEnvioState.Loading) }
            val input =
                EfetivarDoacaoInput(
                    donatarioId = donatarioId,
                    nomeLocal = atual.nomeLocal.trim(),
                    logradouro = atual.logradouro.trim(),
                    bairro = atual.bairro.trim(),
                )
            val result = repository.efetivarDoacao(doacaoId, input)
            result.fold(
                onSuccess = { doacao ->
                    _uiState.update { it.copy(envioState = EfetivarEnvioState.Success(doacao)) }
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(envioState = EfetivarEnvioState.Error(mapearMensagemDeErro(erro)))
                    }
                },
            )
        }
    }
}
