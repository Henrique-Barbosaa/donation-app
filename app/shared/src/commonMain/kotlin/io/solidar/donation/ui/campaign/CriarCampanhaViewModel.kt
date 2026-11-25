package io.solidar.donation.ui.campaign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.domain.model.ItemCampanhaInput
import io.solidar.donation.domain.model.NovaCampanhaInput
import io.solidar.donation.domain.repository.CampanhaRepository
import io.solidar.donation.model.Campaign
import io.solidar.donation.ui.state.mapearMensagemDeErro
import io.solidar.donation.util.CampanhaForm
import io.solidar.donation.util.validarCampanha
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class CriarCampanhaState {
    data object Idle : CriarCampanhaState()

    data object Loading : CriarCampanhaState()

    data class Success(val campanha: Campaign) : CriarCampanhaState()

    data class Error(val message: String) : CriarCampanhaState()
}

data class CriarCampanhaUiState(
    val form: CampanhaForm = CampanhaForm(),
    val envioState: CriarCampanhaState = CriarCampanhaState.Idle,
)

/**
 * ViewModel multiplataforma responsável pelo formulário e envio de uma nova campanha de doação.
 * Em caso de erro, preserva todos os campos preenchidos pelo usuário e exibe um aviso.
 */
class CriarCampanhaViewModel(
    private val repository: CampanhaRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CriarCampanhaUiState())
    val uiState: StateFlow<CriarCampanhaUiState> = _uiState.asStateFlow()

    fun onFormChange(novoForm: CampanhaForm) {
        _uiState.update { atual ->
            atual.copy(
                form = novoForm,
                envioState = if (atual.envioState is CriarCampanhaState.Error) CriarCampanhaState.Idle else atual.envioState,
            )
        }
    }

    fun resetState() {
        _uiState.value = CriarCampanhaUiState()
    }

    fun criarCampanha() {
        val formAtual = _uiState.value.form
        val erros = validarCampanha(formAtual)
        if (!erros.valido) {
            val mensagem = erros.titulo ?: erros.descricao ?: erros.itens ?: "Dados da campanha inválidos."
            _uiState.update { it.copy(envioState = CriarCampanhaState.Error(mensagem)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(envioState = CriarCampanhaState.Loading) }
            val itensInput =
                formAtual.itensPreenchidos.map { item ->
                    ItemCampanhaInput(
                        nome = item.nome.trim(),
                        quantidade = item.quantidade.coerceAtLeast(1),
                    )
                }
            val input =
                NovaCampanhaInput(
                    titulo = formAtual.titulo.trim(),
                    descricao = formAtual.descricao.trim(),
                    itens = itensInput,
                    metaVoluntarios = itensInput.sumOf { it.quantidade }.coerceAtLeast(1),
                )
            val result = repository.criarCampanha(input)
            result.fold(
                onSuccess = { campanha ->
                    _uiState.update { it.copy(envioState = CriarCampanhaState.Success(campanha)) }
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(envioState = CriarCampanhaState.Error(mapearMensagemDeErro(erro)))
                    }
                },
            )
        }
    }
}
