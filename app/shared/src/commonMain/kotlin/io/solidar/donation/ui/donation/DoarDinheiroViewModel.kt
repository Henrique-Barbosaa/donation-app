package io.solidar.donation.ui.donation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.domain.model.NovaDoacaoInput
import io.solidar.donation.domain.repository.DoacaoRepository
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.Doacao
import io.solidar.donation.ui.state.mapearMensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class DoarDinheiroEnvioState {
    data object Idle : DoarDinheiroEnvioState()

    data object Loading : DoarDinheiroEnvioState()

    data class Success(val doacao: Doacao) : DoarDinheiroEnvioState()

    data class Error(val message: String) : DoarDinheiroEnvioState()
}

data class DoarDinheiroUiState(
    val valorReaisTexto: String = "50",
    val chaveCopiada: Boolean = false,
    val envioState: DoarDinheiroEnvioState = DoarDinheiroEnvioState.Idle,
) {
    val valorEmCentavos: Long
        get() = (valorReaisTexto.filter { it.isDigit() }.toLongOrNull() ?: 0L) * 100L

    val valorValido: Boolean
        get() = valorEmCentavos >= 100L
}

/**
 * ViewModel da tela de contribuição financeira (PIX) para uma campanha.
 */
class DoarDinheiroViewModel(
    private val repository: DoacaoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DoarDinheiroUiState())
    val uiState: StateFlow<DoarDinheiroUiState> = _uiState.asStateFlow()

    fun onValorChange(valor: String) {
        val digitos = valor.filter { it.isDigit() }.take(6)
        _uiState.update {
            it.copy(
                valorReaisTexto = digitos,
                envioState = if (it.envioState is DoarDinheiroEnvioState.Error) DoarDinheiroEnvioState.Idle else it.envioState,
            )
        }
    }

    fun selecionarValorSugerido(reais: Int) {
        _uiState.update {
            it.copy(
                valorReaisTexto = reais.toString(),
                envioState = if (it.envioState is DoarDinheiroEnvioState.Error) DoarDinheiroEnvioState.Idle else it.envioState,
            )
        }
    }

    fun marcarChaveCopiada() {
        _uiState.update { it.copy(chaveCopiada = true) }
    }

    fun resetEnvio() {
        _uiState.update { it.copy(envioState = DoarDinheiroEnvioState.Idle) }
    }

    fun confirmarContribuicao(campaign: Campaign) {
        val atual = _uiState.value
        if (!atual.valorValido) {
            _uiState.update {
                it.copy(envioState = DoarDinheiroEnvioState.Error("Informe um valor de pelo menos R$ 1,00."))
            }
            return
        }

        val valorInteiro = atual.valorReaisTexto.toIntOrNull() ?: 0
        viewModelScope.launch {
            _uiState.update { it.copy(envioState = DoarDinheiroEnvioState.Loading) }
            val input =
                NovaDoacaoInput(
                    nome = "Contribuição PIX R$ $valorInteiro,00",
                    quantidade = 1,
                    categoria = "OUTROS",
                    eNovo = true,
                    descricao = "Contribuição financeira via PIX para ${campaign.organizer}",
                    motivo = "Campanha: ${campaign.title}",
                    donatarioId = campaign.instituicaoId,
                )
            val result = repository.criarDoacao(input)
            result.fold(
                onSuccess = { doacao ->
                    _uiState.update { it.copy(envioState = DoarDinheiroEnvioState.Success(doacao)) }
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(envioState = DoarDinheiroEnvioState.Error(mapearMensagemDeErro(erro)))
                    }
                },
            )
        }
    }
}
