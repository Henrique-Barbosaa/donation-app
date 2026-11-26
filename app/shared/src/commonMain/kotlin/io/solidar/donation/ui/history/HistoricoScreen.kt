package io.solidar.donation.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao
import io.solidar.donation.model.DoacaoStatus
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AvisoDadosDesatualizadosBanner
import io.solidar.donation.ui.components.DoacaoCard
import io.solidar.donation.ui.components.EstadoCarregandoView
import io.solidar.donation.ui.components.EstadoErroView
import io.solidar.donation.ui.state.TelaEstado
import io.solidar.donation.util.formatBrl

/**
 * As abas do menu Atividades:
 * - [FEITAS]: doações feitas para campanhas e doações concluídas.
 * - [INTERESSADOS]: ofertas de doação publicadas aguardando interessados/efetivação.
 * - [RECEBIDAS]: doações recebidas (disponível para Pessoa Física e Instituição).
 * - [CAMPANHAS]: campanhas de arrecadação criadas (exclusivo para contas de Instituição).
 */
enum class HistoricoAba(val label: String) {
    FEITAS("Feitas"),
    INTERESSADOS("Interessados"),
    RECEBIDAS("Recebidas"),
    CAMPANHAS("Campanhas"),
}

/**
 * Tela de histórico de atividades separando doações feitas, interessados em ofertas,
 * doações recebidas e (para instituições) campanhas criadas.
 */
@Composable
fun HistoricoScreen(
    estado: TelaEstado<List<Doacao>>,
    selectedTab: HistoricoAba,
    onTabSelected: (HistoricoAba) -> Unit,
    onTentarDeNovo: () -> Unit,
    modifier: Modifier = Modifier,
    onEfetivarDoacao: ((Doacao) -> Unit)? = null,
    onCancelarDoacao: ((Doacao) -> Unit)? = null,
    cancelandoDoacaoId: String? = null,
    avisoAcao: String? = null,
    onLimparAvisoAcao: () -> Unit = {},
    exibirAbaInteressados: Boolean = false,
    exibirAbaCampanhas: Boolean = false,
    estadoMinhasCampanhas: TelaEstado<List<Campaign>> = TelaEstado.Vazio,
    onCampaignClick: (Campaign) -> Unit = {},
    onFinalizarCampanha: ((Campaign) -> Unit)? = null,
    onCancelarCampanha: ((Campaign) -> Unit)? = null,
    processandoCampanhaId: String? = null,
    onRecarregarCampanhas: () -> Unit = onTentarDeNovo,
) {
    var doacaoParaCancelar by remember { mutableStateOf<Doacao?>(null) }
    var campanhaParaFinalizar by remember { mutableStateOf<Campaign?>(null) }
    var campanhaParaCancelar by remember { mutableStateOf<Campaign?>(null) }

    campanhaParaFinalizar?.let { alvo ->
        AlertDialog(
            onDismissRequest = { campanhaParaFinalizar = null },
            title = { Text("Finalizar campanha?") },
            text = {
                Text(
                    "A campanha \"${alvo.title}\" será encerrada e removida da tela inicial, " +
                        "mantendo as doações já recebidas.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        campanhaParaFinalizar = null
                        onFinalizarCampanha?.invoke(alvo)
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = "Confirmar finalização",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { campanhaParaFinalizar = null },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Voltar")
                }
            },
        )
    }

    campanhaParaCancelar?.let { alvo ->
        AlertDialog(
            onDismissRequest = { campanhaParaCancelar = null },
            title = { Text("Cancelar campanha?") },
            text = {
                Text(
                    "A campanha \"${alvo.title}\" será cancelada e os itens ou contribuições " +
                        "vinculados serão devolvidos. Deseja continuar?",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        campanhaParaCancelar = null
                        onCancelarCampanha?.invoke(alvo)
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = "Confirmar cancelamento",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { campanhaParaCancelar = null },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Voltar")
                }
            },
        )
    }

    doacaoParaCancelar?.let { alvo ->
        AlertDialog(
            onDismissRequest = { doacaoParaCancelar = null },
            title = { Text("Cancelar doação?") },
            text = {
                Text(
                    "A oferta \"${alvo.itemTitle}\" será cancelada e removida da lista de doações disponíveis. " +
                        "Deseja continuar?",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        doacaoParaCancelar = null
                        onCancelarDoacao?.invoke(alvo)
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = "Confirmar cancelamento",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { doacaoParaCancelar = null },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Voltar")
                }
            },
        )
    }

    val abasVisiveis =
        buildList {
            add(HistoricoAba.FEITAS)
            if (exibirAbaInteressados) add(HistoricoAba.INTERESSADOS)
            add(HistoricoAba.RECEBIDAS)
            if (exibirAbaCampanhas) add(HistoricoAba.CAMPANHAS)
        }
    val abaEfetiva = if (selectedTab in abasVisiveis) selectedTab else HistoricoAba.FEITAS
    val selectedIndex = abasVisiveis.indexOf(abaEfetiva).coerceAtLeast(0)

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Histórico de doações",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier =
                Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
                    .semantics { heading() },
        )

        HistoricoTabBar(
            abasVisiveis = abasVisiveis,
            selectedIndex = selectedIndex,
            abaEfetiva = abaEfetiva,
            onTabSelected = onTabSelected,
        )

        avisoAcao?.let { aviso ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
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
                        onClick = onLimparAvisoAcao,
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

        if (abaEfetiva == HistoricoAba.CAMPANHAS) {
            MinhasCampanhasHistoricoContent(
                estado = estadoMinhasCampanhas,
                onCampaignClick = onCampaignClick,
                onFinalizarCampanha =
                    if (onFinalizarCampanha != null) {
                        { camp -> campanhaParaFinalizar = camp }
                    } else {
                        null
                    },
                onCancelarCampanha =
                    if (onCancelarCampanha != null) {
                        { camp -> campanhaParaCancelar = camp }
                    } else {
                        null
                    },
                processandoCampanhaId = processandoCampanhaId,
                onTentarDeNovo = onRecarregarCampanhas,
            )
        } else {
            val permiteAcoesPendentes = !exibirAbaInteressados || abaEfetiva == HistoricoAba.INTERESSADOS
            DoacoesHistoricoContent(
                estado = estado,
                selectedTab = abaEfetiva,
                onTentarDeNovo = onTentarDeNovo,
                onEfetivarDoacao = if (permiteAcoesPendentes) onEfetivarDoacao else null,
                onCancelarDoacao =
                    if (permiteAcoesPendentes && onCancelarDoacao != null) {
                        { doacao -> doacaoParaCancelar = doacao }
                    } else {
                        null
                    },
                cancelandoDoacaoId = cancelandoDoacaoId,
            )
        }
    }
}

@Composable
private fun HistoricoTabBar(
    abasVisiveis: List<HistoricoAba>,
    selectedIndex: Int,
    abaEfetiva: HistoricoAba,
    onTabSelected: (HistoricoAba) -> Unit,
) {
    if (abasVisiveis.size >= 4) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedIndex,
            edgePadding = 16.dp,
        ) {
            abasVisiveis.forEach { aba ->
                Tab(
                    selected = abaEfetiva == aba,
                    onClick = { onTabSelected(aba) },
                    text = { Text(aba.label) },
                )
            }
        }
    } else {
        PrimaryTabRow(selectedTabIndex = selectedIndex) {
            abasVisiveis.forEach { aba ->
                Tab(
                    selected = abaEfetiva == aba,
                    onClick = { onTabSelected(aba) },
                    text = { Text(aba.label) },
                )
            }
        }
    }
}

@Composable
private fun DoacoesHistoricoContent(
    estado: TelaEstado<List<Doacao>>,
    selectedTab: HistoricoAba,
    onTentarDeNovo: () -> Unit,
    onEfetivarDoacao: ((Doacao) -> Unit)?,
    onCancelarDoacao: ((Doacao) -> Unit)?,
    cancelandoDoacaoId: String?,
) {
    when (estado) {
        is TelaEstado.Carregando -> {
            EstadoCarregandoView(
                descricao = "Carregando histórico de doações",
                modifier = Modifier.fillMaxSize(),
            )
        }
        is TelaEstado.Vazio -> {
            EmptyHistoricoMessage(
                selectedTab = selectedTab,
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
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = estado.dados, key = { it.id }) { doacao ->
                    DoacaoCard(
                        doacao = doacao,
                        onEfetivarClick = onEfetivarDoacao,
                        onCancelarClick = onCancelarDoacao,
                        cancelando = cancelandoDoacaoId == doacao.id,
                    )
                }
            }
        }
    }
}

@Composable
private fun MinhasCampanhasHistoricoContent(
    estado: TelaEstado<List<Campaign>>,
    onCampaignClick: (Campaign) -> Unit,
    onFinalizarCampanha: ((Campaign) -> Unit)?,
    onCancelarCampanha: ((Campaign) -> Unit)?,
    processandoCampanhaId: String?,
    onTentarDeNovo: () -> Unit,
) {
    when (estado) {
        is TelaEstado.Carregando -> {
            EstadoCarregandoView(
                descricao = "Carregando suas campanhas",
                modifier = Modifier.fillMaxSize(),
            )
        }
        is TelaEstado.Vazio -> {
            EmptyHistoricoMessage(
                selectedTab = HistoricoAba.CAMPANHAS,
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
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = estado.dados, key = { it.id }) { campanha ->
                    MinhaCampanhaCard(
                        campaign = campanha,
                        processando = processandoCampanhaId == campanha.id,
                        onClick = { onCampaignClick(campanha) },
                        onFinalizarClick = onFinalizarCampanha?.let { cb -> { cb(campanha) } },
                        onCancelarClick = onCancelarCampanha?.let { cb -> { cb(campanha) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun MinhaCampanhaCard(
    campaign: Campaign,
    processando: Boolean,
    onClick: () -> Unit,
    onFinalizarClick: (() -> Unit)?,
    onCancelarClick: (() -> Unit)?,
) {
    val ativa = campaign.status.equals("ATIVA", ignoreCase = true)
    val (statusLabel, badgeBg, badgeFg) =
        when (campaign.status.uppercase()) {
            "FINALIZADA" ->
                Triple(
                    "Finalizada",
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer,
                )
            "CANCELADA" ->
                Triple(
                    "Cancelada",
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer,
                )
            else ->
                Triple(
                    "Ativa",
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                )
        }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = campaign.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeBg,
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            if (campaign.descricao.isNotBlank()) {
                Text(
                    text = campaign.descricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (campaign.aceitaDinheiro) {
                val percentual = (campaign.progress * 100).toInt()
                LinearProgressIndicator(
                    progress = { campaign.progress },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .semantics {
                                contentDescription = "Progresso da campanha: $percentual por cento"
                            },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Text(
                    text = "${formatBrl(campaign.raisedInCents)} arrecadados de ${formatBrl(campaign.goalInCents)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (campaign.itensNecessarios.isNotEmpty()) {
                val resumoItens =
                    campaign.itensNecessarios.joinToString(", ") { item ->
                        if (item.quantidadeAlvo != null) "${item.nome} (${item.quantidadeAlvo} un.)" else item.nome
                    }
                Text(
                    text = "Itens solicitados: $resumoItens",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (ativa && (onFinalizarClick != null || onCancelarClick != null)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (onFinalizarClick != null) {
                        Button(
                            onClick = onFinalizarClick,
                            enabled = !processando,
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        ) {
                            Text("Finalizar", fontWeight = FontWeight.Bold)
                        }
                    }
                    if (onCancelarClick != null) {
                        OutlinedButton(
                            onClick = onCancelarClick,
                            enabled = !processando,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        ) {
                            if (processando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            } else {
                                Text("Cancelar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Sobrecarga stateless que filtra uma lista estática e delega para o componente baseado em [TelaEstado]. */
@Composable
fun HistoricoScreen(
    doacoes: List<Doacao>,
    selectedTab: HistoricoAba,
    onTabSelected: (HistoricoAba) -> Unit,
    modifier: Modifier = Modifier,
) {
    val doacoesFiltradas =
        when (selectedTab) {
            HistoricoAba.FEITAS ->
                doacoes.filter { it.direction == DoacaoDirecao.FEITA && it.status != DoacaoStatus.PENDENTE }
            HistoricoAba.INTERESSADOS ->
                doacoes.filter { it.direction == DoacaoDirecao.FEITA && it.status == DoacaoStatus.PENDENTE }
            HistoricoAba.RECEBIDAS -> doacoes.filter { it.direction == DoacaoDirecao.RECEBIDA }
            HistoricoAba.CAMPANHAS -> emptyList()
        }
    val estado =
        if (doacoesFiltradas.isEmpty()) {
            TelaEstado.Vazio
        } else {
            TelaEstado.Sucesso(dados = doacoesFiltradas)
        }

    HistoricoScreen(
        estado = estado,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        onTentarDeNovo = {},
        modifier = modifier,
    )
}

@Composable
private fun EmptyHistoricoMessage(
    selectedTab: HistoricoAba,
    modifier: Modifier = Modifier,
) {
    val message =
        when (selectedTab) {
            HistoricoAba.FEITAS ->
                "Você ainda não concluiu nenhuma doação.\nQuando doar para uma campanha ou concluir uma entrega, ela aparece aqui."
            HistoricoAba.INTERESSADOS ->
                "Nenhuma oferta de doação aguardando interessados.\n" +
                    "Quando publicar um item para doar, você poderá acompanhar os interessados aqui."
            HistoricoAba.RECEBIDAS ->
                "Você ainda não recebeu nenhuma doação.\nQuando receber uma doação, ela aparece aqui."
            HistoricoAba.CAMPANHAS ->
                "Você ainda não criou nenhuma campanha.\nQuando publicar uma, ela aparece aqui."
        }
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun HistoricoScreenFilledPreview() {
    DonationTheme {
        HistoricoScreen(
            doacoes =
                listOf(
                    Doacao(
                        id = "preview-1",
                        itemTitle = "Item de Exemplo",
                        counterpartName = "Instituição Parceira",
                        direction = DoacaoDirecao.FEITA,
                        status = DoacaoStatus.CONCLUIDA,
                        dateLabel = "Registrada recentemente",
                    ),
                ),
            selectedTab = HistoricoAba.FEITAS,
            onTabSelected = {},
        )
    }
}

@Preview
@Composable
private fun HistoricoScreenEmptyPreview() {
    DonationTheme {
        HistoricoScreen(
            doacoes = emptyList(),
            selectedTab = HistoricoAba.RECEBIDAS,
            onTabSelected = {},
        )
    }
}

@Preview
@Composable
private fun HistoricoScreenDarkPreview() {
    DonationTheme(darkTheme = true) {
        HistoricoScreen(
            doacoes =
                listOf(
                    Doacao(
                        id = "preview-1",
                        itemTitle = "Item de Exemplo",
                        counterpartName = "Instituição Parceira",
                        direction = DoacaoDirecao.FEITA,
                        status = DoacaoStatus.CONCLUIDA,
                        dateLabel = "Registrada recentemente",
                    ),
                ),
            selectedTab = HistoricoAba.FEITAS,
            onTabSelected = {},
        )
    }
}
