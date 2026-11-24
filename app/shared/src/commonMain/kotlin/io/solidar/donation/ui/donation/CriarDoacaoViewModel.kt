package io.solidar.donation.ui.donation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.domain.model.NovaDoacaoInput
import io.solidar.donation.domain.repository.DoacaoRepository
import io.solidar.donation.model.Doacao
import io.solidar.donation.ui.state.mapearMensagemDeErro
import io.solidar.donation.util.DoacaoForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class CriarDoacaoState {
    data object Idle : CriarDoacaoState()

    data object Loading : CriarDoacaoState()

    data class Success(val doacao: Doacao) : CriarDoacaoState()

    data class Error(val message: String) : CriarDoacaoState()
}

data class CriarDoacaoUiState(
    val form: DoacaoForm = DoacaoForm(),
    val submissionState: CriarDoacaoState = CriarDoacaoState.Idle,
)

/**
 * ViewModel multiplataforma para criação de uma nova doação.
 * Mantém o estado do formulário no [StateFlow] (quando salvar falhar, mantém os dados na tela e mostra o aviso).
 * Recebe [DoacaoRepository] pelo construtor.
 */
class CriarDoacaoViewModel(
    private val repository: DoacaoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CriarDoacaoUiState())
    val uiState: StateFlow<CriarDoacaoUiState> = _uiState.asStateFlow()

    fun atualizarForm(novoForm: DoacaoForm) {
        _uiState.update { it.copy(form = novoForm) }
    }

    fun resetState() {
        _uiState.value = CriarDoacaoUiState()
    }

    fun submeterDoacao() {
        val currentForm = _uiState.value.form
        criarDoacao(
            nome = currentForm.nome,
            quantidade = currentForm.quantidade,
            categoria = currentForm.categoria,
            eNovo = currentForm.eNovo,
            descricao = currentForm.descricao,
            motivo = currentForm.motivo,
        )
    }

    fun criarDoacao(
        nome: String,
        quantidade: Int,
        categoria: String,
        eNovo: Boolean,
        descricao: String,
        motivo: String,
    ) {
        if (nome.isBlank()) {
            _uiState.update {
                it.copy(submissionState = CriarDoacaoState.Error("O título ou nome do item é obrigatório."))
            }
            return
        }
        if (quantidade < 1) {
            _uiState.update {
                it.copy(submissionState = CriarDoacaoState.Error("A quantidade deve ser pelo menos 1."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(submissionState = CriarDoacaoState.Loading) }

            val input =
                NovaDoacaoInput(
                    nome = nome.trim(),
                    quantidade = quantidade,
                    categoria = categoria,
                    eNovo = eNovo,
                    descricao = descricao,
                    motivo = motivo,
                )

            val result = repository.criarDoacao(input = input)
            result.fold(
                onSuccess = { doacao ->
                    _uiState.update { it.copy(submissionState = CriarDoacaoState.Success(doacao)) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            submissionState = CriarDoacaoState.Error(mapearMensagemDeErro(error)),
                        )
                    }
                },
            )
        }
    }
}
