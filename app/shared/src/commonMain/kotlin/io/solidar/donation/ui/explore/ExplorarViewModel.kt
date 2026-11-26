package io.solidar.donation.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.domain.repository.DoacaoRepository
import io.solidar.donation.model.DoacaoDisponivel
import io.solidar.donation.ui.state.TelaEstado
import io.solidar.donation.ui.state.mapearMensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExplorarUiState(
    val busca: String = "",
    val categoriaSelecionada: String? = null,
    val estadoLista: TelaEstado<List<DoacaoDisponivel>> = TelaEstado.Carregando,
    val interessesIds: Set<String> = emptySet(),
    val processandoInteresseId: String? = null,
    val avisoInteresse: String? = null,
)

/**
 * ViewModel multiplataforma do feed público de doações disponíveis (aba Explorar)
 * e manifestação de interesse em itens.
 */
class ExplorarViewModel(
    private val repository: DoacaoRepository,
    carregarAoIniciar: Boolean = true,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            ExplorarUiState(
                interessesIds = repository.listarInteressesIds(),
            ),
        )
    val uiState: StateFlow<ExplorarUiState> = _uiState.asStateFlow()

    init {
        if (carregarAoIniciar) {
            carregarDoacoes()
        }
    }

    fun onBuscaChange(texto: String) {
        _uiState.update { it.copy(busca = texto, avisoInteresse = null) }
    }

    fun buscar() {
        carregarDoacoes()
    }

    fun onCategoriaSelecionada(categoria: String?) {
        val novaCategoria = if (_uiState.value.categoriaSelecionada == categoria) null else categoria
        _uiState.update { it.copy(categoriaSelecionada = novaCategoria, avisoInteresse = null) }
        carregarDoacoes()
    }

    fun limparAviso() {
        _uiState.update { it.copy(avisoInteresse = null) }
    }

    fun limparEstadoSessao() {
        _uiState.update {
            it.copy(
                interessesIds = emptySet(),
                processandoInteresseId = null,
                avisoInteresse = null,
            )
        }
    }

    fun carregarDoacoes() {
        val atual = _uiState.value
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    estadoLista = TelaEstado.Carregando,
                    interessesIds = repository.listarInteressesIds(),
                )
            }
            val result =
                repository.listarDoacoesDisponiveis(
                    categoria = atual.categoriaSelecionada,
                    busca = atual.busca.trim().ifBlank { null },
                )
            result.fold(
                onSuccess = { resultado ->
                    val novoEstado =
                        if (resultado.dados.isEmpty()) {
                            TelaEstado.Vazio
                        } else {
                            TelaEstado.Sucesso(
                                dados = resultado.dados,
                                desatualizado = resultado.desatualizado,
                            )
                        }
                    _uiState.update { it.copy(estadoLista = novoEstado) }
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(estadoLista = TelaEstado.Erro(mapearMensagemDeErro(erro)))
                    }
                },
            )
        }
    }

    fun alternarInteresse(doacaoId: String) {
        if (_uiState.value.processandoInteresseId != null) return
        val jaInteressado = _uiState.value.interessesIds.contains(doacaoId)
        executarAcaoInteresse(doacaoId = doacaoId, remover = jaInteressado, mensagem = null)
    }

    fun registrarInteresseComMensagem(
        doacaoId: String,
        mensagem: String,
    ) {
        if (_uiState.value.processandoInteresseId != null) return
        executarAcaoInteresse(doacaoId = doacaoId, remover = false, mensagem = mensagem)
    }

    private fun executarAcaoInteresse(
        doacaoId: String,
        remover: Boolean,
        mensagem: String?,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(processandoInteresseId = doacaoId, avisoInteresse = null) }
            val result =
                if (remover) {
                    repository.removerInteresse(doacaoId)
                } else {
                    repository.registrarInteresse(doacaoId, mensagem)
                }
            result.fold(
                onSuccess = {
                    val novosInteresses =
                        if (remover) {
                            _uiState.value.interessesIds - doacaoId
                        } else {
                            _uiState.value.interessesIds + doacaoId
                        }
                    val feedback =
                        if (remover) {
                            "Interesse removido com sucesso."
                        } else {
                            "Interesse registrado! O doador foi notificado."
                        }
                    _uiState.update {
                        it.copy(
                            interessesIds = novosInteresses,
                            processandoInteresseId = null,
                            avisoInteresse = feedback,
                        )
                    }
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            processandoInteresseId = null,
                            avisoInteresse = mapearMensagemDeErro(erro),
                        )
                    }
                },
            )
        }
    }
}
