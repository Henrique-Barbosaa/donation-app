package io.solidar.donation.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.domain.error.FalhaDominio
import io.solidar.donation.domain.repository.CampanhaRepository
import io.solidar.donation.domain.repository.DoacaoRepository
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.model.DoacaoStatus
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
    val estadoMinhasCampanhas: TelaEstado<List<Campaign>> = TelaEstado.Vazio,
    val cancelandoDoacaoId: String? = null,
    val processandoCampanhaId: String? = null,
    val avisoAcao: String? = null,
)

/**
 * ViewModel multiplataforma da tela de histórico de doações (feitas, interessados, recebidas e campanhas).
 * Recebe [DoacaoRepository] e opcionalmente [CampanhaRepository] pelo construtor (injeção de dependência).
 */
class HistoricoViewModel(
    private val repository: DoacaoRepository,
    private val campanhaRepository: CampanhaRepository? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoricoUiState())
    val uiState: StateFlow<HistoricoUiState> = _uiState.asStateFlow()

    private var todasDoacoesCache: List<Doacao>? = null
    private var dadosDesatualizados: Boolean = false

    init {
        carregarHistorico(carregarCampanhas = false)
    }

    fun carregarHistorico(carregarCampanhas: Boolean = false) {
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
            if (carregarCampanhas) {
                carregarMinhasCampanhasInterno()
            } else {
                _uiState.update { it.copy(estadoMinhasCampanhas = TelaEstado.Vazio) }
            }
        }
    }

    fun carregarMinhasCampanhas() {
        viewModelScope.launch {
            carregarMinhasCampanhasInterno()
        }
    }

    fun cancelarDoacao(
        doacaoId: String,
        onSucesso: () -> Unit = {},
    ) {
        if (_uiState.value.cancelandoDoacaoId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(cancelandoDoacaoId = doacaoId, avisoAcao = null) }
            val result = repository.cancelarDoacao(doacaoId)
            result.fold(
                onSuccess = {
                    todasDoacoesCache = todasDoacoesCache?.filterNot { it.id == doacaoId }
                    val novoEstado = calcularEstadoFiltrado(_uiState.value.abaSelecionada)
                    _uiState.update { atual ->
                        atual.copy(
                            estadoLista = novoEstado ?: atual.estadoLista,
                            cancelandoDoacaoId = null,
                            avisoAcao = "Doação cancelada com sucesso.",
                        )
                    }
                    onSucesso()
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            cancelandoDoacaoId = null,
                            avisoAcao = mapearMensagemDeErro(erro),
                        )
                    }
                },
            )
        }
    }

    fun limparAvisoAcao() {
        _uiState.update { it.copy(avisoAcao = null) }
    }

    fun finalizarCampanha(
        campanhaId: String,
        onSucesso: () -> Unit = {},
    ) {
        val repoCampanha = campanhaRepository ?: return
        if (_uiState.value.processandoCampanhaId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(processandoCampanhaId = campanhaId, avisoAcao = null) }
            val result = repoCampanha.finalizarCampanha(campanhaId)
            result.fold(
                onSuccess = {
                    atualizarStatusCampanhaLocal(campanhaId, "FINALIZADA")
                    _uiState.update {
                        it.copy(
                            processandoCampanhaId = null,
                            avisoAcao = "Campanha finalizada com sucesso.",
                        )
                    }
                    onSucesso()
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            processandoCampanhaId = null,
                            avisoAcao = mapearMensagemDeErro(erro),
                        )
                    }
                },
            )
        }
    }

    fun cancelarCampanha(
        campanhaId: String,
        onSucesso: () -> Unit = {},
    ) {
        val repoCampanha = campanhaRepository ?: return
        if (_uiState.value.processandoCampanhaId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(processandoCampanhaId = campanhaId, avisoAcao = null) }
            val result = repoCampanha.cancelarCampanha(campanhaId)
            result.fold(
                onSuccess = {
                    atualizarStatusCampanhaLocal(campanhaId, "CANCELADA")
                    _uiState.update {
                        it.copy(
                            processandoCampanhaId = null,
                            avisoAcao = "Campanha cancelada e doações vinculadas estornadas.",
                        )
                    }
                    onSucesso()
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            processandoCampanhaId = null,
                            avisoAcao = mapearMensagemDeErro(erro),
                        )
                    }
                },
            )
        }
    }

    private fun atualizarStatusCampanhaLocal(
        campanhaId: String,
        novoStatus: String,
    ) {
        val estadoAtual = _uiState.value.estadoMinhasCampanhas
        if (estadoAtual is TelaEstado.Sucesso) {
            val atualizadas =
                estadoAtual.dados.map { camp ->
                    if (camp.id == campanhaId) camp.copy(status = novoStatus) else camp
                }
            _uiState.update {
                it.copy(
                    estadoMinhasCampanhas = TelaEstado.Sucesso(atualizadas, estadoAtual.desatualizado),
                )
            }
        }
    }

    fun limparEstadoSessao() {
        todasDoacoesCache = null
        dadosDesatualizados = false
        _uiState.value = HistoricoUiState(estadoLista = TelaEstado.Vazio)
    }

    private suspend fun carregarMinhasCampanhasInterno() {
        val repoCampanha = campanhaRepository ?: return
        _uiState.update { it.copy(estadoMinhasCampanhas = TelaEstado.Carregando) }
        val result = repoCampanha.listarMinhasCampanhas()
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
                _uiState.update { it.copy(estadoMinhasCampanhas = novoEstado) }
            },
            onFailure = { erro ->
                val estadoFalha =
                    if (erro is FalhaDominio.DadosInvalidos) {
                        TelaEstado.Vazio
                    } else {
                        TelaEstado.Erro(mapearMensagemDeErro(erro))
                    }
                _uiState.update { it.copy(estadoMinhasCampanhas = estadoFalha) }
            },
        )
    }

    fun selecionarAba(aba: HistoricoAba) {
        val novoEstado = calcularEstadoFiltrado(aba)
        _uiState.update { atual ->
            atual.copy(
                abaSelecionada = aba,
                estadoLista = novoEstado ?: atual.estadoLista,
            )
        }
    }

    private fun atualizarListaFiltrada(aba: HistoricoAba) {
        val novoEstado = calcularEstadoFiltrado(aba) ?: return
        _uiState.update { it.copy(estadoLista = novoEstado) }
    }

    private fun calcularEstadoFiltrado(aba: HistoricoAba): TelaEstado<List<Doacao>>? {
        val todas = todasDoacoesCache ?: return null
        val filtradas =
            when (aba) {
                HistoricoAba.FEITAS ->
                    todas.filter { it.direction == DoacaoDirecao.FEITA && it.status != DoacaoStatus.PENDENTE }
                HistoricoAba.INTERESSADOS ->
                    todas.filter { it.direction == DoacaoDirecao.FEITA && it.status == DoacaoStatus.PENDENTE }
                HistoricoAba.RECEBIDAS ->
                    todas.filter { it.direction == DoacaoDirecao.RECEBIDA }
                HistoricoAba.CAMPANHAS -> return null
            }
        return if (filtradas.isEmpty()) {
            TelaEstado.Vazio
        } else {
            TelaEstado.Sucesso(
                dados = filtradas,
                desatualizado = dadosDesatualizados,
            )
        }
    }
}
