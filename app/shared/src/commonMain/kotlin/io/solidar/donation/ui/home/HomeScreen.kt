package io.solidar.donation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.DoacaoDisponivel
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AvisoDadosDesatualizadosBanner
import io.solidar.donation.ui.components.CampaignCard
import io.solidar.donation.ui.components.DemonstrarInteresseDialog
import io.solidar.donation.ui.components.DoacaoDisponivelCard
import io.solidar.donation.ui.components.EstadoCarregandoView
import io.solidar.donation.ui.components.EstadoErroView
import io.solidar.donation.ui.state.TelaEstado

/** As duas abas da tela inicial: campanhas que pedem doações e campanhas/itens que oferecem doações. */
enum class InicioAba {
    PEDIDOS,
    OFERTAS,
}

/**
 * Tela inicial que separa, por menu superior de abas (mesmo padrão do Histórico),
 * as campanhas que pedem doações e os itens/ofertas disponíveis para doar,
 * tratando exaustivamente os estados de [TelaEstado] (`Carregando`, `Vazio`, `Sucesso` e `Erro`).
 */
@Composable
fun HomeScreen(
    estado: TelaEstado<List<Campaign>>,
    onCampaignClick: (Campaign) -> Unit,
    onTentarDeNovo: () -> Unit,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
    titulo: String = "Campanhas abertas",
    onNavigateToCreateCampaign: (() -> Unit)? = null,
    exibirAbasTipo: Boolean = false,
    abaSelecionada: InicioAba = InicioAba.PEDIDOS,
    onAbaSelecionada: (InicioAba) -> Unit = {},
    estadoDoacoesDisponiveis: TelaEstado<List<DoacaoDisponivel>> = TelaEstado.Vazio,
    interessesIds: Set<String> = emptySet(),
    processandoInteresseId: String? = null,
    avisoInteresse: String? = null,
    onAlternarInteresse: (String) -> Unit = {},
    onRegistrarInteresseComMensagem: ((String, String) -> Unit)? = null,
    onRecarregarDoacoes: () -> Unit = {},
    onLimparAvisoInteresse: () -> Unit = {},
    onNavigateToCreateDonation: (() -> Unit)? = null,
    usuarioLogadoId: Int? = null,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            if ((!exibirAbasTipo || abaSelecionada == InicioAba.PEDIDOS) && onNavigateToCreateCampaign != null) {
                FilledTonalButton(
                    onClick = onNavigateToCreateCampaign,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = "+ Campanha",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (exibirAbasTipo) {
            PrimaryTabRow(selectedTabIndex = abaSelecionada.ordinal) {
                Tab(
                    selected = abaSelecionada == InicioAba.PEDIDOS,
                    onClick = { onAbaSelecionada(InicioAba.PEDIDOS) },
                    text = { Text("Pedem doações") },
                )
                Tab(
                    selected = abaSelecionada == InicioAba.OFERTAS,
                    onClick = { onAbaSelecionada(InicioAba.OFERTAS) },
                    text = { Text("Oferecem doações") },
                )
            }
        }

        if (!exibirAbasTipo || abaSelecionada == InicioAba.PEDIDOS) {
            CampanhasPedidosContent(
                estado = estado,
                onCampaignClick = onCampaignClick,
                onTentarDeNovo = onTentarDeNovo,
                onNavigateToCreateCampaign = onNavigateToCreateCampaign,
                wide = wide,
            )
        } else {
            OfertasDoacoesContent(
                estado = estadoDoacoesDisponiveis,
                interessesIds = interessesIds,
                processandoInteresseId = processandoInteresseId,
                avisoInteresse = avisoInteresse,
                onAlternarInteresse = onAlternarInteresse,
                onRegistrarInteresseComMensagem = onRegistrarInteresseComMensagem,
                onTentarDeNovo = onRecarregarDoacoes,
                onLimparAviso = onLimparAvisoInteresse,
                onNavigateToCreateDonation = onNavigateToCreateDonation,
                usuarioLogadoId = usuarioLogadoId,
                wide = wide,
            )
        }
    }
}

/** Sobrecarga conveniente para exibir uma lista direta já resolvida. */
@Composable
fun HomeScreen(
    campaigns: List<Campaign>,
    onCampaignClick: (Campaign) -> Unit,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
) {
    val estado =
        if (campaigns.isEmpty()) {
            TelaEstado.Vazio
        } else {
            TelaEstado.Sucesso(dados = campaigns)
        }
    HomeScreen(
        estado = estado,
        onCampaignClick = onCampaignClick,
        onTentarDeNovo = {},
        modifier = modifier,
        wide = wide,
    )
}

@Composable
private fun CampanhasPedidosContent(
    estado: TelaEstado<List<Campaign>>,
    onCampaignClick: (Campaign) -> Unit,
    onTentarDeNovo: () -> Unit,
    onNavigateToCreateCampaign: (() -> Unit)?,
    wide: Boolean,
) {
    when (estado) {
        is TelaEstado.Carregando -> {
            EstadoCarregandoView(
                descricao = "Carregando campanhas",
                modifier = Modifier.fillMaxSize(),
            )
        }
        is TelaEstado.Vazio -> {
            EmptyCampaignsMessage(
                onCreateCampaign = onNavigateToCreateCampaign,
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
            CampaignListContent(
                campaigns = estado.dados,
                onCampaignClick = onCampaignClick,
                wide = wide,
            )
        }
    }
}

@Composable
private fun OfertasDoacoesContent(
    estado: TelaEstado<List<DoacaoDisponivel>>,
    interessesIds: Set<String>,
    processandoInteresseId: String?,
    avisoInteresse: String?,
    onAlternarInteresse: (String) -> Unit,
    onRegistrarInteresseComMensagem: ((String, String) -> Unit)?,
    onTentarDeNovo: () -> Unit,
    onLimparAviso: () -> Unit,
    onNavigateToCreateDonation: (() -> Unit)?,
    usuarioLogadoId: Int?,
    wide: Boolean,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        avisoInteresse?.let { aviso ->
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

        when (estado) {
            is TelaEstado.Carregando -> {
                EstadoCarregandoView(
                    descricao = "Carregando doações oferecidas",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is TelaEstado.Vazio -> {
                EmptyOfertasMessage(
                    onCreateDonation = onNavigateToCreateDonation,
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
                OfertasListContent(
                    doacoes = estado.dados,
                    interessesIds = interessesIds,
                    processandoInteresseId = processandoInteresseId,
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
private fun CampaignListContent(
    campaigns: List<Campaign>,
    onCampaignClick: (Campaign) -> Unit,
    wide: Boolean,
) {
    if (wide) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = campaigns, key = { it.id }) { campaign ->
                CampaignCard(
                    campaign = campaign,
                    onClick = onCampaignClick,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = campaigns, key = { it.id }) { campaign ->
                CampaignCard(
                    campaign = campaign,
                    onClick = onCampaignClick,
                )
            }
        }
    }
}

@Composable
private fun OfertasListContent(
    doacoes: List<DoacaoDisponivel>,
    interessesIds: Set<String>,
    processandoInteresseId: String?,
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
        val jaInteressado = item.id in interessesIds
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
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = doacoes, key = { it.id }) { doacao ->
                DoacaoDisponivelCard(
                    doacao = doacao,
                    interessado = doacao.id in interessesIds,
                    processando = processandoInteresseId == doacao.id,
                    onAlternarInteresse = { handleCliqueInteresse(doacao) },
                    ehCriador = usuarioLogadoId != null && doacao.doadorId == usuarioLogadoId,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = doacoes, key = { it.id }) { doacao ->
                DoacaoDisponivelCard(
                    doacao = doacao,
                    interessado = doacao.id in interessesIds,
                    processando = processandoInteresseId == doacao.id,
                    onAlternarInteresse = { handleCliqueInteresse(doacao) },
                    ehCriador = usuarioLogadoId != null && doacao.doadorId == usuarioLogadoId,
                )
            }
        }
    }
}

@Composable
private fun EmptyCampaignsMessage(
    onCreateCampaign: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Nenhuma campanha por aqui ainda.\nAssim que alguém criar uma, ela aparece nesta lista.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (onCreateCampaign != null) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onCreateCampaign,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text(
                        text = "Criar campanha",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyOfertasMessage(
    onCreateDonation: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Nenhuma oferta de doação disponível no momento.\nUse o botão + Doar para publicar um item.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (onCreateDonation != null) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onCreateDonation,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text(
                        text = "Oferecer doação",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenFilledPreview() {
    val previewCampanhas =
        listOf(
            Campaign(
                id = "preview-1",
                title = "Campanha de Exemplo",
                organizer = "Instituição Parceira",
                goalInCents = 100_000L,
                raisedInCents = 50_000L,
            ),
        )
    DonationTheme {
        HomeScreen(
            estado = TelaEstado.Sucesso(dados = previewCampanhas),
            onCampaignClick = {},
            onTentarDeNovo = {},
            exibirAbasTipo = true,
        )
    }
}

@Preview(widthDp = 840, heightDp = 600)
@Composable
private fun HomeScreenWidePreview() {
    val previewCampanhas =
        listOf(
            Campaign(
                id = "preview-1",
                title = "Campanha de Exemplo",
                organizer = "Instituição Parceira",
                goalInCents = 100_000L,
                raisedInCents = 50_000L,
            ),
        )
    DonationTheme {
        HomeScreen(
            estado = TelaEstado.Sucesso(dados = previewCampanhas),
            onCampaignClick = {},
            onTentarDeNovo = {},
            wide = true,
            exibirAbasTipo = true,
        )
    }
}

@Preview
@Composable
private fun HomeScreenEmptyPreview() {
    DonationTheme {
        HomeScreen(
            estado = TelaEstado.Vazio,
            onCampaignClick = {},
            onTentarDeNovo = {},
        )
    }
}

@Preview
@Composable
private fun HomeScreenErrorPreview() {
    DonationTheme(darkTheme = true) {
        HomeScreen(
            estado = TelaEstado.Erro("Sem conexão com o servidor. Verifique sua internet."),
            onCampaignClick = {},
            onTentarDeNovo = {},
        )
    }
}
