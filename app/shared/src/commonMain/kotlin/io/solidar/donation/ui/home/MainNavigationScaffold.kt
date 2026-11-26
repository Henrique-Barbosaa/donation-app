package io.solidar.donation.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import io.solidar.donation.di.AppContainer
import io.solidar.donation.domain.model.PerfilUsuario
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.UserSession
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.ui.components.PlaceholderScreen
import io.solidar.donation.ui.explore.ExplorarScreen
import io.solidar.donation.ui.explore.ExplorarUiState
import io.solidar.donation.ui.explore.ExplorarViewModel
import io.solidar.donation.ui.history.HistoricoAba
import io.solidar.donation.ui.history.HistoricoScreen
import io.solidar.donation.ui.history.HistoricoViewModel
import io.solidar.donation.ui.profile.PerfilScreen
import io.solidar.donation.ui.profile.PerfilViewModel
import io.solidar.donation.ui.state.TelaEstado

enum class MainTab(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Início", AppIcons.Home),
    EXPLORE("Explorar", AppIcons.Search),
    FAVORITES("Favoritas", AppIcons.Favorite),
    ACTIVITIES("Atividades", AppIcons.Notifications),
    PROFILE("Perfil", AppIcons.Person),
}

/**
 * Shell principal pós-login. Lê o estado de [CampanhasViewModel], [HistoricoViewModel] e [PerfilViewModel]
 * com [collectAsStateWithLifecycle], sem manter estado de negócio em `remember`.
 */
@Composable
fun MainNavigationScaffold(
    onNavigateToCreateDonation: () -> Unit,
    onLogout: () -> Unit,
    onCampaignClick: (Campaign) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCreateCampaign: (() -> Unit)? = null,
    onEfetivarDoacao: ((Doacao) -> Unit)? = null,
    onNavigateToEditarPerfil: () -> Unit = {},
    onNavigateToAlterarSenha: () -> Unit = {},
    activeSession: UserSession? = null,
    campanhasViewModel: CampanhasViewModel = viewModel { AppContainer.default.createCampanhasViewModel() },
    historicoViewModel: HistoricoViewModel = viewModel { AppContainer.default.createHistoricoViewModel() },
    explorarViewModel: ExplorarViewModel = viewModel { AppContainer.default.createExplorarViewModel() },
    perfilViewModel: PerfilViewModel = viewModel { AppContainer.default.createPerfilViewModel() },
) {
    val wide =
        currentWindowAdaptiveInfo()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val campanhasUiState by campanhasViewModel.uiState.collectAsStateWithLifecycle()
    val historicoUiState by historicoViewModel.uiState.collectAsStateWithLifecycle()
    val explorarUiState by explorarViewModel.uiState.collectAsStateWithLifecycle()
    val perfilUiState by perfilViewModel.uiState.collectAsStateWithLifecycle()

    val ehInstituicao = activeSession?.tipo == "INSTITUICAO"

    MainNavigationContent(
        wide = wide,
        selectedTab = campanhasUiState.abaPrincipal,
        onTabSelected = { novaAba ->
            campanhasViewModel.selecionarAbaPrincipal(novaAba)
            if (novaAba == MainTab.ACTIVITIES) {
                historicoViewModel.carregarHistorico(carregarCampanhas = ehInstituicao)
            }
        },
        inicioTab = campanhasUiState.abaInicio,
        onInicioTabSelected = campanhasViewModel::selecionarAbaInicio,
        historicoTab = historicoUiState.abaSelecionada,
        onHistoricoTabSelected = historicoViewModel::selecionarAba,
        estadoCampanhas = campanhasUiState.estadoLista,
        favoritosIds = campanhasUiState.favoritosIds,
        onRecarregarCampanhas = campanhasViewModel::carregarCampanhas,
        estadoHistorico = historicoUiState.estadoLista,
        estadoMinhasCampanhas = historicoUiState.estadoMinhasCampanhas,
        cancelandoDoacaoId = historicoUiState.cancelandoDoacaoId,
        processandoCampanhaId = historicoUiState.processandoCampanhaId,
        avisoHistorico = historicoUiState.avisoAcao,
        onRecarregarHistorico = { historicoViewModel.carregarHistorico(carregarCampanhas = ehInstituicao) },
        onRecarregarMinhasCampanhas = historicoViewModel::carregarMinhasCampanhas,
        onCancelarDoacao = { doacao ->
            historicoViewModel.cancelarDoacao(doacao.id) {
                explorarViewModel.carregarDoacoes()
            }
        },
        onFinalizarCampanha = { campanha ->
            historicoViewModel.finalizarCampanha(campanha.id) {
                campanhasViewModel.carregarCampanhas()
            }
        },
        onCancelarCampanha = { campanha ->
            historicoViewModel.cancelarCampanha(campanha.id) {
                campanhasViewModel.carregarCampanhas()
                historicoViewModel.carregarHistorico(carregarCampanhas = ehInstituicao)
            }
        },
        onLimparAvisoHistorico = historicoViewModel::limparAvisoAcao,
        explorarUiState = explorarUiState,
        onExplorarBuscaChange = explorarViewModel::onBuscaChange,
        onExplorarBuscar = explorarViewModel::buscar,
        onExplorarCategoriaSelecionada = explorarViewModel::onCategoriaSelecionada,
        onAlternarInteresse = explorarViewModel::alternarInteresse,
        onRegistrarInteresseComMensagem = explorarViewModel::registrarInteresseComMensagem,
        onRecarregarExplorar = explorarViewModel::carregarDoacoes,
        onLimparAvisoExplorar = explorarViewModel::limparAviso,
        activeSession = activeSession,
        onNavigateToCreateCampaign = onNavigateToCreateCampaign,
        onEfetivarDoacao = onEfetivarDoacao,
        totalDoacoesFeitas = historicoUiState.totalFeitas,
        totalDoacoesRecebidas = historicoUiState.totalRecebidas,
        estadoPerfil = perfilUiState.estadoPerfil,
        mensagemFeedbackPerfil = perfilUiState.mensagemFeedback,
        onRecarregarPerfil = perfilViewModel::carregarPerfil,
        onEditarPerfilClick = {
            perfilViewModel.prepararEdicaoPerfil()
            onNavigateToEditarPerfil()
        },
        onAlterarSenhaClick = {
            perfilViewModel.prepararAlteracaoSenha()
            onNavigateToAlterarSenha()
        },
        onCampaignClick = onCampaignClick,
        onNavigateToCreateDonation = onNavigateToCreateDonation,
        onLogout = onLogout,
        modifier = modifier,
    )
}

/**
 * Conteúdo stateless: janela compacta usa barra inferior + FAB; janela média
 * ou expandida (≥ 600 dp) usa NavigationRail lateral e grade de cartões.
 */
@Composable
fun MainNavigationContent(
    wide: Boolean,
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    historicoTab: HistoricoAba,
    onHistoricoTabSelected: (HistoricoAba) -> Unit,
    onCampaignClick: (Campaign) -> Unit,
    onNavigateToCreateDonation: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    inicioTab: InicioAba = InicioAba.PEDIDOS,
    onInicioTabSelected: (InicioAba) -> Unit = {},
    estadoCampanhas: TelaEstado<List<Campaign>> = TelaEstado.Vazio,
    favoritosIds: Set<String> = emptySet(),
    onRecarregarCampanhas: () -> Unit = {},
    estadoHistorico: TelaEstado<List<Doacao>> = TelaEstado.Vazio,
    estadoMinhasCampanhas: TelaEstado<List<Campaign>> = TelaEstado.Vazio,
    cancelandoDoacaoId: String? = null,
    processandoCampanhaId: String? = null,
    avisoHistorico: String? = null,
    onRecarregarHistorico: () -> Unit = {},
    onRecarregarMinhasCampanhas: () -> Unit = {},
    onCancelarDoacao: ((Doacao) -> Unit)? = null,
    onFinalizarCampanha: ((Campaign) -> Unit)? = null,
    onCancelarCampanha: ((Campaign) -> Unit)? = null,
    onLimparAvisoHistorico: () -> Unit = {},
    explorarUiState: ExplorarUiState = ExplorarUiState(estadoLista = TelaEstado.Vazio),
    onExplorarBuscaChange: (String) -> Unit = {},
    onExplorarBuscar: () -> Unit = {},
    onExplorarCategoriaSelecionada: (String?) -> Unit = {},
    onAlternarInteresse: (String) -> Unit = {},
    onRegistrarInteresseComMensagem: ((String, String) -> Unit)? = null,
    onRecarregarExplorar: () -> Unit = {},
    onLimparAvisoExplorar: () -> Unit = {},
    activeSession: UserSession? = null,
    onNavigateToCreateCampaign: (() -> Unit)? = null,
    onEfetivarDoacao: ((Doacao) -> Unit)? = null,
    totalDoacoesFeitas: Int = 0,
    totalDoacoesRecebidas: Int = 0,
    estadoPerfil: TelaEstado<PerfilUsuario> = TelaEstado.Carregando,
    mensagemFeedbackPerfil: String? = null,
    onRecarregarPerfil: () -> Unit = {},
    onEditarPerfilClick: () -> Unit = {},
    onAlterarSenhaClick: () -> Unit = {},
) {
    var exibirSeletorCadastro by rememberSaveable { mutableStateOf(false) }
    val podeCriarCampanha = onNavigateToCreateCampaign != null
    val onBotaoMaisClick: () -> Unit = {
        if (podeCriarCampanha) {
            exibirSeletorCadastro = true
        } else {
            onNavigateToCreateDonation()
        }
    }

    if (exibirSeletorCadastro && onNavigateToCreateCampaign != null) {
        SeletorTipoCadastroDialog(
            onDismiss = { exibirSeletorCadastro = false },
            onCadastrarArrecadacao = {
                exibirSeletorCadastro = false
                onNavigateToCreateCampaign()
            },
            onCadastrarDoacao = {
                exibirSeletorCadastro = false
                onNavigateToCreateDonation()
            },
        )
    }

    if (wide) {
        Row(modifier = modifier.fillMaxSize()) {
            NavigationRail(
                header = {
                    FloatingActionButton(onClick = onBotaoMaisClick) {
                        Icon(
                            AppIcons.Add,
                            contentDescription =
                                if (podeCriarCampanha) {
                                    "Criar campanha ou doação"
                                } else {
                                    "Criar doação"
                                },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                },
            ) {
                MainTab.entries.forEach { tab ->
                    NavigationRailItem(
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
            Scaffold { padding ->
                TabContent(
                    tab = selectedTab,
                    wide = true,
                    inicioTab = inicioTab,
                    onInicioTabSelected = onInicioTabSelected,
                    historicoTab = historicoTab,
                    onHistoricoTabSelected = onHistoricoTabSelected,
                    estadoCampanhas = estadoCampanhas,
                    favoritosIds = favoritosIds,
                    onRecarregarCampanhas = onRecarregarCampanhas,
                    estadoHistorico = estadoHistorico,
                    estadoMinhasCampanhas = estadoMinhasCampanhas,
                    cancelandoDoacaoId = cancelandoDoacaoId,
                    processandoCampanhaId = processandoCampanhaId,
                    avisoHistorico = avisoHistorico,
                    onRecarregarHistorico = onRecarregarHistorico,
                    onRecarregarMinhasCampanhas = onRecarregarMinhasCampanhas,
                    onCancelarDoacao = onCancelarDoacao,
                    onFinalizarCampanha = onFinalizarCampanha,
                    onCancelarCampanha = onCancelarCampanha,
                    onLimparAvisoHistorico = onLimparAvisoHistorico,
                    explorarUiState = explorarUiState,
                    onExplorarBuscaChange = onExplorarBuscaChange,
                    onExplorarBuscar = onExplorarBuscar,
                    onExplorarCategoriaSelecionada = onExplorarCategoriaSelecionada,
                    onAlternarInteresse = onAlternarInteresse,
                    onRegistrarInteresseComMensagem = onRegistrarInteresseComMensagem,
                    onRecarregarExplorar = onRecarregarExplorar,
                    onLimparAvisoExplorar = onLimparAvisoExplorar,
                    activeSession = activeSession,
                    onNavigateToCreateCampaign = onNavigateToCreateCampaign,
                    onNavigateToCreateDonation = onNavigateToCreateDonation,
                    onEfetivarDoacao = onEfetivarDoacao,
                    totalDoacoesFeitas = totalDoacoesFeitas,
                    totalDoacoesRecebidas = totalDoacoesRecebidas,
                    estadoPerfil = estadoPerfil,
                    mensagemFeedbackPerfil = mensagemFeedbackPerfil,
                    onRecarregarPerfil = onRecarregarPerfil,
                    onEditarPerfilClick = onEditarPerfilClick,
                    onAlterarSenhaClick = onAlterarSenhaClick,
                    onCampaignClick = onCampaignClick,
                    onLogout = onLogout,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onBotaoMaisClick,
                    icon = { Icon(AppIcons.Add, contentDescription = null) },
                    text = { Text(if (podeCriarCampanha) "Criar" else "Doar") },
                    modifier =
                        Modifier.semantics {
                            contentDescription =
                                if (podeCriarCampanha) {
                                    "Criar campanha ou doação"
                                } else {
                                    "Criar nova doação"
                                }
                        },
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { onTabSelected(tab) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            },
        ) { padding ->
            TabContent(
                tab = selectedTab,
                wide = false,
                inicioTab = inicioTab,
                onInicioTabSelected = onInicioTabSelected,
                historicoTab = historicoTab,
                onHistoricoTabSelected = onHistoricoTabSelected,
                estadoCampanhas = estadoCampanhas,
                favoritosIds = favoritosIds,
                onRecarregarCampanhas = onRecarregarCampanhas,
                estadoHistorico = estadoHistorico,
                estadoMinhasCampanhas = estadoMinhasCampanhas,
                cancelandoDoacaoId = cancelandoDoacaoId,
                processandoCampanhaId = processandoCampanhaId,
                avisoHistorico = avisoHistorico,
                onRecarregarHistorico = onRecarregarHistorico,
                onRecarregarMinhasCampanhas = onRecarregarMinhasCampanhas,
                onCancelarDoacao = onCancelarDoacao,
                onFinalizarCampanha = onFinalizarCampanha,
                onCancelarCampanha = onCancelarCampanha,
                onLimparAvisoHistorico = onLimparAvisoHistorico,
                explorarUiState = explorarUiState,
                onExplorarBuscaChange = onExplorarBuscaChange,
                onExplorarBuscar = onExplorarBuscar,
                onExplorarCategoriaSelecionada = onExplorarCategoriaSelecionada,
                onAlternarInteresse = onAlternarInteresse,
                onRegistrarInteresseComMensagem = onRegistrarInteresseComMensagem,
                onRecarregarExplorar = onRecarregarExplorar,
                onLimparAvisoExplorar = onLimparAvisoExplorar,
                activeSession = activeSession,
                onNavigateToCreateCampaign = onNavigateToCreateCampaign,
                onNavigateToCreateDonation = onNavigateToCreateDonation,
                onEfetivarDoacao = onEfetivarDoacao,
                totalDoacoesFeitas = totalDoacoesFeitas,
                totalDoacoesRecebidas = totalDoacoesRecebidas,
                estadoPerfil = estadoPerfil,
                mensagemFeedbackPerfil = mensagemFeedbackPerfil,
                onRecarregarPerfil = onRecarregarPerfil,
                onEditarPerfilClick = onEditarPerfilClick,
                onAlterarSenhaClick = onAlterarSenhaClick,
                onCampaignClick = onCampaignClick,
                onLogout = onLogout,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun TabContent(
    tab: MainTab,
    wide: Boolean,
    inicioTab: InicioAba,
    onInicioTabSelected: (InicioAba) -> Unit,
    historicoTab: HistoricoAba,
    onHistoricoTabSelected: (HistoricoAba) -> Unit,
    estadoCampanhas: TelaEstado<List<Campaign>>,
    favoritosIds: Set<String>,
    onRecarregarCampanhas: () -> Unit,
    estadoHistorico: TelaEstado<List<Doacao>>,
    estadoMinhasCampanhas: TelaEstado<List<Campaign>>,
    cancelandoDoacaoId: String?,
    processandoCampanhaId: String?,
    avisoHistorico: String?,
    onRecarregarHistorico: () -> Unit,
    onRecarregarMinhasCampanhas: () -> Unit,
    onCancelarDoacao: ((Doacao) -> Unit)?,
    onFinalizarCampanha: ((Campaign) -> Unit)?,
    onCancelarCampanha: ((Campaign) -> Unit)?,
    onLimparAvisoHistorico: () -> Unit,
    explorarUiState: ExplorarUiState,
    onExplorarBuscaChange: (String) -> Unit,
    onExplorarBuscar: () -> Unit,
    onExplorarCategoriaSelecionada: (String?) -> Unit,
    onAlternarInteresse: (String) -> Unit,
    onRegistrarInteresseComMensagem: ((String, String) -> Unit)?,
    onRecarregarExplorar: () -> Unit,
    onLimparAvisoExplorar: () -> Unit,
    activeSession: UserSession?,
    onNavigateToCreateCampaign: (() -> Unit)?,
    onNavigateToCreateDonation: () -> Unit,
    onEfetivarDoacao: ((Doacao) -> Unit)?,
    totalDoacoesFeitas: Int,
    totalDoacoesRecebidas: Int,
    estadoPerfil: TelaEstado<PerfilUsuario>,
    mensagemFeedbackPerfil: String?,
    onRecarregarPerfil: () -> Unit,
    onEditarPerfilClick: () -> Unit,
    onAlterarSenhaClick: () -> Unit,
    onCampaignClick: (Campaign) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Crossfade(targetState = tab, modifier = modifier) { current ->
        when (current) {
            MainTab.HOME ->
                HomeScreen(
                    estado = estadoCampanhas,
                    onCampaignClick = onCampaignClick,
                    onTentarDeNovo = onRecarregarCampanhas,
                    wide = wide,
                    onNavigateToCreateCampaign = onNavigateToCreateCampaign,
                    exibirAbasTipo = true,
                    abaSelecionada = inicioTab,
                    onAbaSelecionada = onInicioTabSelected,
                    estadoDoacoesDisponiveis = explorarUiState.estadoLista,
                    interessesIds = explorarUiState.interessesIds,
                    processandoInteresseId = explorarUiState.processandoInteresseId,
                    avisoInteresse = explorarUiState.avisoInteresse,
                    onAlternarInteresse = onAlternarInteresse,
                    onRegistrarInteresseComMensagem = onRegistrarInteresseComMensagem,
                    onRecarregarDoacoes = onRecarregarExplorar,
                    onLimparAvisoInteresse = onLimparAvisoExplorar,
                    onNavigateToCreateDonation = onNavigateToCreateDonation,
                    usuarioLogadoId = activeSession?.id,
                )
            MainTab.FAVORITES -> {
                val estadoFavoritas: TelaEstado<List<Campaign>> =
                    when (estadoCampanhas) {
                        is TelaEstado.Carregando -> TelaEstado.Carregando
                        is TelaEstado.Erro -> estadoCampanhas
                        is TelaEstado.Vazio -> TelaEstado.Vazio
                        is TelaEstado.Sucesso -> {
                            val filtradas = estadoCampanhas.dados.filter { favoritosIds.contains(it.id) }
                            if (filtradas.isEmpty()) {
                                TelaEstado.Vazio
                            } else {
                                TelaEstado.Sucesso(
                                    dados = filtradas,
                                    desatualizado = estadoCampanhas.desatualizado,
                                )
                            }
                        }
                    }

                if (estadoFavoritas is TelaEstado.Vazio) {
                    PlaceholderScreen(
                        title = "Nenhuma campanha salva",
                        subtitle = "As campanhas que você marcar nos favoritos aparecerão aqui.",
                        icon = AppIcons.Favorite,
                    )
                } else {
                    HomeScreen(
                        estado = estadoFavoritas,
                        onCampaignClick = onCampaignClick,
                        onTentarDeNovo = onRecarregarCampanhas,
                        wide = wide,
                        titulo = "Campanhas favoritas",
                    )
                }
            }
            MainTab.EXPLORE ->
                ExplorarScreen(
                    uiState = explorarUiState,
                    onBuscaChange = onExplorarBuscaChange,
                    onBuscar = onExplorarBuscar,
                    onCategoriaSelecionada = onExplorarCategoriaSelecionada,
                    onAlternarInteresse = onAlternarInteresse,
                    onRegistrarInteresseComMensagem = onRegistrarInteresseComMensagem,
                    onTentarDeNovo = onRecarregarExplorar,
                    onLimparAviso = onLimparAvisoExplorar,
                    usuarioLogadoId = activeSession?.id,
                    wide = wide,
                )
            MainTab.ACTIVITIES ->
                HistoricoScreen(
                    estado = estadoHistorico,
                    selectedTab = historicoTab,
                    onTabSelected = onHistoricoTabSelected,
                    onTentarDeNovo = onRecarregarHistorico,
                    onEfetivarDoacao = onEfetivarDoacao,
                    onCancelarDoacao = onCancelarDoacao,
                    cancelandoDoacaoId = cancelandoDoacaoId,
                    avisoAcao = avisoHistorico,
                    onLimparAvisoAcao = onLimparAvisoHistorico,
                    exibirAbaInteressados = true,
                    exibirAbaCampanhas = activeSession?.tipo == "INSTITUICAO",
                    estadoMinhasCampanhas = estadoMinhasCampanhas,
                    onCampaignClick = onCampaignClick,
                    onFinalizarCampanha = onFinalizarCampanha,
                    onCancelarCampanha = onCancelarCampanha,
                    processandoCampanhaId = processandoCampanhaId,
                    onRecarregarCampanhas = onRecarregarMinhasCampanhas,
                )
            MainTab.PROFILE ->
                PerfilScreen(
                    estado = estadoPerfil,
                    wide = wide,
                    mensagemFeedback = mensagemFeedbackPerfil,
                    totalDoacoesFeitas = totalDoacoesFeitas,
                    totalDoacoesRecebidas = totalDoacoesRecebidas,
                    totalFavoritas = favoritosIds.size,
                    onEditarPerfilClick = onEditarPerfilClick,
                    onAlterarSenhaClick = onAlterarSenhaClick,
                    onTentarDeNovo = onRecarregarPerfil,
                    onLogout = onLogout,
                )
        }
    }
}

@Composable
private fun SeletorTipoCadastroDialog(
    onDismiss: () -> Unit,
    onCadastrarArrecadacao: () -> Unit,
    onCadastrarDoacao: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "O que deseja cadastrar?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = "Escolha entre criar uma campanha para arrecadar ou publicar itens para doar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                OpcaoCadastroCard(
                    titulo = "Campanha de arrecadação",
                    descricao = "Solicite itens com quantidades individuais ou contribuições via PIX.",
                    emoji = "🎯",
                    onClick = onCadastrarArrecadacao,
                )

                OpcaoCadastroCard(
                    titulo = "Campanha de doação",
                    descricao = "Publique itens disponíveis para pessoas ou instituições retirarem.",
                    emoji = "📦",
                    onClick = onCadastrarDoacao,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(
                            text = "Cancelar",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OpcaoCadastroCard(
    titulo: String,
    descricao: String,
    emoji: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clearAndSetSemantics {},
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = descricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
private fun MainCompactPreview() {
    DonationTheme {
        MainNavigationContent(
            wide = false,
            selectedTab = MainTab.HOME,
            onTabSelected = {},
            historicoTab = HistoricoAba.FEITAS,
            onHistoricoTabSelected = {},
            onCampaignClick = {},
            onNavigateToCreateDonation = {},
            onLogout = {},
        )
    }
}

@Preview(widthDp = 900, heightDp = 600)
@Composable
private fun MainWidePreview() {
    DonationTheme(darkTheme = true) {
        MainNavigationContent(
            wide = true,
            selectedTab = MainTab.HOME,
            onTabSelected = {},
            historicoTab = HistoricoAba.FEITAS,
            onHistoricoTabSelected = {},
            onCampaignClick = {},
            onNavigateToCreateDonation = {},
            onLogout = {},
        )
    }
}
