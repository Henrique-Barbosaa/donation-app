package io.solidar.donation.ui.donation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.data.remote.dto.CriarDoacaoRequest
import io.solidar.donation.data.remote.dto.DoacaoResponse
import io.solidar.donation.data.remote.dto.ItemRequest
import io.solidar.donation.data.repository.DoacaoRepositoryImpl
import io.solidar.donation.domain.repository.DoacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CriarDoacaoState {
    object Idle : CriarDoacaoState()

    object Loading : CriarDoacaoState()

    data class Success(val doacao: DoacaoResponse) : CriarDoacaoState()

    data class Error(val message: String) : CriarDoacaoState()
}

class CriarDoacaoViewModel(
    private val repository: DoacaoRepository = DoacaoRepositoryImpl(),
) : ViewModel() {
    private val _state = MutableStateFlow<CriarDoacaoState>(CriarDoacaoState.Idle)
    val state: StateFlow<CriarDoacaoState> = _state.asStateFlow()

    fun resetState() {
        _state.value = CriarDoacaoState.Idle
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
            _state.value = CriarDoacaoState.Error("O título ou nome do item é obrigatório.")
            return
        }
        if (quantidade < 1) {
            _state.value = CriarDoacaoState.Error("A quantidade deve ser pelo menos 1.")
            return
        }

        viewModelScope.launch {
            _state.value = CriarDoacaoState.Loading

            val item =
                ItemRequest(
                    nome = nome.trim(),
                    quantidade = quantidade,
                    descricao = descricao.trim().ifBlank { null },
                    motivo = motivo.trim().ifBlank { null },
                    eNovo = if (eNovo) "S" else "N",
                    categoria = categoria.uppercase(),
                )

            val request = CriarDoacaoRequest(itens = listOf(item))

            val result = repository.criarDoacao(request = request)
            result.fold(
                onSuccess = { doacao ->
                    _state.value = CriarDoacaoState.Success(doacao)
                },
                onFailure = { error ->
                    _state.value =
                        CriarDoacaoState.Error(
                            error.message ?: "Não foi possível cadastrar a doação. Verifique sua conexão e se a sessão está ativa.",
                        )
                },
            )
        }
    }
}
