package io.solidar.donation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.domain.repository.CampanhaRepository
import io.solidar.donation.model.Campaign
import io.solidar.donation.ui.state.TelaEstado
import io.solidar.donation.ui.state.mapearMensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CampanhasUiState(
    val abaPrincipal: MainTab = MainTab.HOME,
    val abaInicio: InicioAba = InicioAba.PEDIDOS,
    val estadoLista: TelaEstado<List<Campaign>> = TelaEstado.Carregando,
    val favoritosIds: Set<String> = emptySet(),
    val cancelandoCampanha: Boolean = false,
    val erroCancelamento: String? = null,
) {
    val campanhasAtuais: List<Campaign>
        get() = (estadoLista as? TelaEstado.Sucesso)?.dados.orEmpty()
}

/**
 * ViewModel multiplataforma responsável pelo estado da listagem de campanhas,
 * favoritos e navegação entre abas principais.
 *
 * Recebe [CampanhaRepository] pelo construtor (injeção de dependência).
 */
class CampanhasViewModel(
    private val repository: CampanhaRepository,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            CampanhasUiState(
                favoritosIds = repository.listarFavoritosIds(),
            ),
        )
    val uiState: StateFlow<CampanhasUiState> = _uiState.asStateFlow()

    init {
        carregarCampanhas()
    }

    fun carregarCampanhas() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    estadoLista = TelaEstado.Carregando,
                    favoritosIds = repository.listarFavoritosIds(),
                )
            }
            val result = repository.listarCampanhas()
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
                        it.copy(
                            estadoLista = TelaEstado.Erro(mapearMensagemDeErro(erro)),
                        )
                    }
                },
            )
        }
    }

    fun selecionarAbaPrincipal(tab: MainTab) {
        _uiState.update { it.copy(abaPrincipal = tab) }
    }

    fun selecionarAbaInicio(aba: InicioAba) {
        _uiState.update { it.copy(abaInicio = aba) }
    }

    fun alternarFavorito(campanhaId: String) {
        val atualizados = repository.alternarFavorito(campanhaId)
        _uiState.update { it.copy(favoritosIds = atualizados) }
    }

    fun finalizarCampanha(
        campanhaId: String,
        onSucesso: () -> Unit,
    ) {
        if (_uiState.value.cancelandoCampanha) return
        viewModelScope.launch {
            _uiState.update { it.copy(cancelandoCampanha = true, erroCancelamento = null) }
            val result = repository.finalizarCampanha(campanhaId)
            result.fold(
                onSuccess = {
                    val restantes = _uiState.value.campanhasAtuais.filterNot { it.id == campanhaId }
                    val novoEstado =
                        if (restantes.isEmpty()) {
                            TelaEstado.Vazio
                        } else {
                            TelaEstado.Sucesso(dados = restantes)
                        }
                    _uiState.update {
                        it.copy(
                            estadoLista = novoEstado,
                            cancelandoCampanha = false,
                        )
                    }
                    onSucesso()
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            cancelandoCampanha = false,
                            erroCancelamento = mapearMensagemDeErro(erro),
                        )
                    }
                },
            )
        }
    }

    fun cancelarCampanha(
        campanhaId: String,
        onSucesso: () -> Unit,
    ) {
        if (_uiState.value.cancelandoCampanha) return
        viewModelScope.launch {
            _uiState.update { it.copy(cancelandoCampanha = true, erroCancelamento = null) }
            val result = repository.cancelarCampanha(campanhaId)
            result.fold(
                onSuccess = {
                    val restantes = _uiState.value.campanhasAtuais.filterNot { it.id == campanhaId }
                    val novoEstado =
                        if (restantes.isEmpty()) {
                            TelaEstado.Vazio
                        } else {
                            TelaEstado.Sucesso(dados = restantes)
                        }
                    _uiState.update {
                        it.copy(
                            estadoLista = novoEstado,
                            favoritosIds = repository.listarFavoritosIds(),
                            cancelandoCampanha = false,
                        )
                    }
                    onSucesso()
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            cancelandoCampanha = false,
                            erroCancelamento = mapearMensagemDeErro(erro),
                        )
                    }
                },
            )
        }
    }

    fun limparErroCancelamento() {
        _uiState.update { it.copy(erroCancelamento = null) }
    }

    fun limparEstadoSessao() {
        _uiState.update {
            it.copy(
                abaPrincipal = MainTab.HOME,
                abaInicio = InicioAba.PEDIDOS,
                favoritosIds = emptySet(),
                erroCancelamento = null,
            )
        }
    }

    fun buscarCampanhaPorId(id: String): Campaign? {
        return _uiState.value.campanhasAtuais.firstOrNull { it.id == id }
            ?: sampleCampaigns.firstOrNull { it.id == id }
    }
}
