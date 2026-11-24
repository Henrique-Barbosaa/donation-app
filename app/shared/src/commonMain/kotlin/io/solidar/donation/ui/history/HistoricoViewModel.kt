package io.solidar.donation.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.domain.repository.DoacaoRepository
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.ui.state.TelaEstado
import io.solidar.donation.ui.state.mapearMensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoricoUiState(
    val abaSelecionada: HistoricoAba = HistoricoAba.FEITAS,
    val estadoLista: TelaEstado<List<Doacao>> = TelaEstado.Carregando,
)

/**
 * ViewModel multiplataforma da tela de histórico de doações (feitas e recebidas).
 * Recebe [DoacaoRepository] pelo construtor (injeção de dependência).
 */
class HistoricoViewModel(
    private val repository: DoacaoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoricoUiState())
    val uiState: StateFlow<HistoricoUiState> = _uiState.asStateFlow()

    private var todasDoacoesCache: List<Doacao>? = null
    private var dadosDesatualizados: Boolean = false

    init {
        carregarHistorico()
    }

    fun carregarHistorico() {
        viewModelScope.launch {
            _uiState.update { it.copy(estadoLista = TelaEstado.Carregando) }
            val result = repository.listarHistoricoDoacoes()
            result.fold(
                onSuccess = { resultado ->
                    todasDoacoesCache = resultado.dados
                    dadosDesatualizados = resultado.desatualizado
                    atualizarListaFiltrada(_uiState.value.abaSelecionada)
                },
                onFailure = { erro ->
                    todasDoacoesCache = null
                    _uiState.update {
                        it.copy(
                            estadoLista = TelaEstado.Erro(mapearMensagemDeErro(erro)),
                        )
                    }
                },
            )
        }
    }

    fun selecionarAba(aba: HistoricoAba) {
        _uiState.update { it.copy(abaSelecionada = aba) }
        if (todasDoacoesCache != null) {
            atualizarListaFiltrada(aba)
        }
    }

    private fun atualizarListaFiltrada(aba: HistoricoAba) {
        val todas = todasDoacoesCache ?: return
        val direcao =
            when (aba) {
                HistoricoAba.FEITAS -> DoacaoDirecao.FEITA
                HistoricoAba.RECEBIDAS -> DoacaoDirecao.RECEBIDA
            }
        val filtradas = todas.filter { it.direction == direcao }
        val novoEstado =
            if (filtradas.isEmpty()) {
                TelaEstado.Vazio
            } else {
                TelaEstado.Sucesso(
                    dados = filtradas,
                    desatualizado = dadosDesatualizados,
                )
            }
        _uiState.update { it.copy(estadoLista = novoEstado) }
    }
}
