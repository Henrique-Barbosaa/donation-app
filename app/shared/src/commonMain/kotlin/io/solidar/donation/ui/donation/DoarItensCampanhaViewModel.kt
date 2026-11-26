package io.solidar.donation.ui.donation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.solidar.donation.domain.model.NovaDoacaoInput
import io.solidar.donation.domain.repository.DoacaoRepository
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.CampaignItem
import io.solidar.donation.model.Doacao
import io.solidar.donation.ui.state.mapearMensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class DoarItensEnvioState {
    data object Idle : DoarItensEnvioState()

    data object Loading : DoarItensEnvioState()

    data class Success(val doacao: Doacao) : DoarItensEnvioState()

    data class Error(val message: String) : DoarItensEnvioState()
}

data class DoarItensCampanhaUiState(
    val itensPendentes: List<CampaignItem> = emptyList(),
    val quantidadesSelecionadas: Map<String, Int> = emptyMap(),
    val observacao: String = "",
    val envioState: DoarItensEnvioState = DoarItensEnvioState.Idle,
) {
    val totalItensSelecionados: Int
        get() = quantidadesSelecionadas.size

    val podeConfirmar: Boolean
        get() = quantidadesSelecionadas.isNotEmpty() && envioState !is DoarItensEnvioState.Loading
}

/**
 * ViewModel para seleção dos itens solicitados por uma campanha que ainda possuem
 * quantidade disponível para doar.
 */
class DoarItensCampanhaViewModel(
    private val repository: DoacaoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DoarItensCampanhaUiState())
    val uiState: StateFlow<DoarItensCampanhaUiState> = _uiState.asStateFlow()

    fun carregarItensPendentes(campaign: Campaign) {
        val pendentes = calcularItensPendentes(campaign)
        _uiState.update {
            it.copy(
                itensPendentes = pendentes,
                quantidadesSelecionadas = it.quantidadesSelecionadas.filterKeys { id -> pendentes.any { p -> p.id == id } },
                envioState = DoarItensEnvioState.Idle,
            )
        }
    }

    fun alternarSelecaoItem(itemId: String) {
        _uiState.update { atual ->
            val mapa = atual.quantidadesSelecionadas.toMutableMap()
            if (mapa.containsKey(itemId)) {
                mapa.remove(itemId)
            } else {
                mapa[itemId] = 1
            }
            atual.copy(
                quantidadesSelecionadas = mapa,
                envioState = if (atual.envioState is DoarItensEnvioState.Error) DoarItensEnvioState.Idle else atual.envioState,
            )
        }
    }

    fun alterarQuantidade(
        itemId: String,
        delta: Int,
    ) {
        _uiState.update { atual ->
            val qtdAtual = atual.quantidadesSelecionadas[itemId] ?: return@update atual
            val limiteMaximo = atual.itensPendentes.firstOrNull { it.id == itemId }?.quantidadeAlvo
            val novaBruta = (qtdAtual + delta).coerceAtLeast(1)
            val novaQtd = if (limiteMaximo != null) novaBruta.coerceAtMost(limiteMaximo) else novaBruta
            atual.copy(
                quantidadesSelecionadas = atual.quantidadesSelecionadas + (itemId to novaQtd),
            )
        }
    }

    fun onObservacaoChange(valor: String) {
        _uiState.update { it.copy(observacao = valor) }
    }

    fun resetEnvio() {
        _uiState.update { it.copy(envioState = DoarItensEnvioState.Idle) }
    }

    fun confirmarDoacao(campaign: Campaign) {
        val atual = _uiState.value
        val selecionados =
            atual.itensPendentes.mapNotNull { item ->
                val qtd = atual.quantidadesSelecionadas[item.id] ?: return@mapNotNull null
                item to qtd
            }
        if (selecionados.isEmpty()) {
            _uiState.update {
                it.copy(envioState = DoarItensEnvioState.Error("Selecione pelo menos um item para doar."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(envioState = DoarItensEnvioState.Loading) }
            val inputs =
                selecionados.map { (item, qtd) ->
                    NovaDoacaoInput(
                        nome = item.nome,
                        quantidade = qtd,
                        categoria = inferirCategoria(item.nome),
                        eNovo = false,
                        descricao = atual.observacao.trim().ifBlank { "Item doado para a campanha ${campaign.title}" },
                        motivo = "Campanha: ${campaign.title}",
                        donatarioId = campaign.instituicaoId,
                    )
                }
            val result =
                repository.doarItensParaCampanha(
                    campanhaId = campaign.id,
                    campanhaTitulo = campaign.title,
                    itens = inputs,
                    itensIds = selecionados.map { it.first.id }.toSet(),
                    donatarioId = campaign.instituicaoId,
                    quantidadesPorItemId = selecionados.associate { (item, qtd) -> item.id to qtd },
                )
            result.fold(
                onSuccess = { doacao ->
                    _uiState.update {
                        it.copy(
                            itensPendentes = calcularItensPendentes(campaign),
                            quantidadesSelecionadas = emptyMap(),
                            envioState = DoarItensEnvioState.Success(doacao),
                        )
                    }
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(envioState = DoarItensEnvioState.Error(mapearMensagemDeErro(erro)))
                    }
                },
            )
        }
    }

    private fun calcularItensPendentes(campaign: Campaign): List<CampaignItem> {
        val jaDoadosLegado = repository.listarItensDoadosCampanha(campaign.id)
        val quantidadesDoadas = repository.listarQuantidadesDoadasCampanha(campaign.id)
        return campaign.itensNecessarios.mapNotNull { item ->
            val alvo = item.quantidadeAlvo
            if (alvo != null && alvo > 0) {
                val doado = quantidadesDoadas[item.id] ?: 0
                val restante = (alvo - doado).coerceAtLeast(0)
                if (restante <= 0) {
                    null
                } else {
                    val desc =
                        when {
                            doado > 0 -> "Faltam $restante de $alvo un."
                            restante == 1 -> "1 unidade"
                            else -> "$restante unidades"
                        }
                    item.copy(
                        quantidadeDescricao = desc,
                        quantidadeAlvo = restante,
                    )
                }
            } else {
                if (item.id in jaDoadosLegado) null else item
            }
        }
    }

    private fun inferirCategoria(nomeItem: String): String {
        val lower = nomeItem.lowercase()
        return when {
            lower.contains("alimento") || lower.contains("cesta") -> "ALIMENTOS"
            lower.contains("roupa") || lower.contains("casaco") || lower.contains("cobertor") -> "ROUPAS"
            lower.contains("livro") || lower.contains("caderno") || lower.contains("estojo") -> "LIVROS"
            lower.contains("brinquedo") -> "BRINQUEDOS"
            else -> "OUTROS"
        }
    }
}
