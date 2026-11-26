package io.solidar.donation.ui.explore

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.solidar.donation.model.DoacaoDisponivel
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.ui.components.AvisoDadosDesatualizadosBanner
import io.solidar.donation.ui.components.DemonstrarInteresseDialog
import io.solidar.donation.ui.components.DoacaoDisponivelCard
import io.solidar.donation.ui.components.EstadoCarregandoView
import io.solidar.donation.ui.components.EstadoErroView
import io.solidar.donation.ui.donation.CATEGORIAS_PADRAO
import io.solidar.donation.ui.state.TelaEstado

/**
 * Aba Explorar: feed público de doações disponíveis com busca, filtro por categoria,
 * manifestação de interesse e tratamento exaustivo dos 4 estados de [TelaEstado].
 */
@Composable
fun ExplorarScreen(
    uiState: ExplorarUiState,
    onBuscaChange: (String) -> Unit,
    onBuscar: () -> Unit,
    onCategoriaSelecionada: (String?) -> Unit,
    onAlternarInteresse: (String) -> Unit,
    onTentarDeNovo: () -> Unit,
    onLimparAviso: () -> Unit,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
    onRegistrarInteresseComMensagem: ((String, String) -> Unit)? = null,
    usuarioLogadoId: Int? = null,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Explorar doações",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier =
                Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
                    .semantics { heading() },
        )

        OutlinedTextField(
            value = uiState.busca,
            onValueChange = onBuscaChange,
            placeholder = { Text("Buscar por item, categoria ou doador...") },
            leadingIcon = {
                Icon(
                    imageVector = AppIcons.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                TextButton(
                    onClick = onBuscar,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = "Buscar",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onBuscar() }),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        )

        CategoriaFiltroRow(
            categoriaSelecionada = uiState.categoriaSelecionada,
            onCategoriaSelecionada = onCategoriaSelecionada,
        )

        uiState.avisoInteresse?.let { aviso ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = aviso,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = onLimparAviso,
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(
                            text = "OK",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        when (val estado = uiState.estadoLista) {
            is TelaEstado.Carregando -> {
                EstadoCarregandoView(
                    descricao = "Carregando doações disponíveis",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is TelaEstado.Vazio -> {
                EmptyExplorarMessage(
                    temFiltro = uiState.busca.isNotBlank() || uiState.categoriaSelecionada != null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is TelaEstado.Erro -> {
                EstadoErroView(
                    mensagem = estado.mensagem,
                    onTentarDeNovo = onTentarDeNovo,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is TelaEstado.Sucesso -> {
                if (estado.desatualizado) {
                    AvisoDadosDesatualizadosBanner(onRecarregar = onTentarDeNovo)
                }
                DoacoesDisponiveisList(
                    doacoes = estado.dados,
                    interessesIds = uiState.interessesIds,
                    processandoId = uiState.processandoInteresseId,
                    onAlternarInteresse = onAlternarInteresse,
                    onRegistrarInteresseComMensagem = onRegistrarInteresseComMensagem,
                    usuarioLogadoId = usuarioLogadoId,
                    wide = wide,
                )
            }
        }
    }
}

@Composable
private fun CategoriaFiltroRow(
    categoriaSelecionada: String?,
    onCategoriaSelecionada: (String?) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FiltroChip(
            label = "Todas",
            selected = categoriaSelecionada == null,
            onClick = { onCategoriaSelecionada(null) },
        )
        CATEGORIAS_PADRAO.forEach { cat ->
            FiltroChip(
                label = cat,
                selected = categoriaSelecionada == cat,
                onClick = { onCategoriaSelecionada(cat) },
            )
        }
    }
}

@Composable
private fun FiltroChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        modifier =
            Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    shape = RoundedCornerShape(20.dp),
                )
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (selected) {
                Icon(
                    imageVector = AppIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DoacoesDisponiveisList(
    doacoes: List<DoacaoDisponivel>,
    interessesIds: Set<String>,
    processandoId: String?,
    onAlternarInteresse: (String) -> Unit,
    onRegistrarInteresseComMensagem: ((String, String) -> Unit)?,
    usuarioLogadoId: Int?,
    wide: Boolean,
) {
    var doacaoParaMensagem by remember { mutableStateOf<DoacaoDisponivel?>(null) }

    doacaoParaMensagem?.let { alvo ->
        DemonstrarInteresseDialog(
            doacao = alvo,
            onDismiss = { doacaoParaMensagem = null },
            onConfirmar = { msg ->
                doacaoParaMensagem = null
                onRegistrarInteresseComMensagem?.invoke(alvo.id, msg) ?: onAlternarInteresse(alvo.id)
            },
        )
    }

    val handleCliqueInteresse: (DoacaoDisponivel) -> Unit = { item ->
        val jaInteressado = interessesIds.contains(item.id)
        if (!jaInteressado && onRegistrarInteresseComMensagem != null) {
            doacaoParaMensagem = item
        } else {
            onAlternarInteresse(item.id)
        }
    }

    if (wide) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = doacoes, key = { it.id }) { doacao ->
                DoacaoDisponivelCard(
                    doacao = doacao,
                    interessado = interessesIds.contains(doacao.id),
                    processando = processandoId == doacao.id,
                    onAlternarInteresse = { handleCliqueInteresse(doacao) },
                    ehCriador = usuarioLogadoId != null && doacao.doadorId == usuarioLogadoId,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = doacoes, key = { it.id }) { doacao ->
                DoacaoDisponivelCard(
                    doacao = doacao,
                    interessado = interessesIds.contains(doacao.id),
                    processando = processandoId == doacao.id,
                    onAlternarInteresse = { handleCliqueInteresse(doacao) },
                    ehCriador = usuarioLogadoId != null && doacao.doadorId == usuarioLogadoId,
                )
            }
        }
    }
}

@Composable
private fun EmptyExplorarMessage(
    temFiltro: Boolean,
    modifier: Modifier = Modifier,
) {
    val mensagem =
        if (temFiltro) {
            "Nenhuma doação encontrada para este filtro.\nTente limpar a busca ou escolher outra categoria."
        } else {
            "Nenhuma doação disponível no momento.\nUse o botão Doar para publicar o primeiro item!"
        }
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun ExplorarScreenFilledPreview() {
    DonationTheme {
        ExplorarScreen(
            uiState =
                ExplorarUiState(
                    estadoLista =
                        TelaEstado.Sucesso(
                            listOf(
                                DoacaoDisponivel(
                                    id = "1",
                                    itemTitle = "Cesta básica completa",
                                    quantidade = 3,
                                    categoria = "Alimentos",
                                    condicao = "Novo",
                                    descricao = "Arroz, feijão, macarrão e óleo dentro da validade.",
                                    motivo = "Ação comunitária",
                                    nomeDoador = "João Batista",
                                ),
                            ),
                        ),
                ),
            onBuscaChange = {},
            onBuscar = {},
            onCategoriaSelecionada = {},
            onAlternarInteresse = {},
            onTentarDeNovo = {},
            onLimparAviso = {},
        )
    }
}
