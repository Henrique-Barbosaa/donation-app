package io.solidar.donation.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import io.solidar.donation.di.AppContainer
import io.solidar.donation.domain.model.PerfilUsuario
import io.solidar.donation.model.Campaign
import io.solidar.donation.model.Doacao
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.ui.components.PlaceholderScreen
import io.solidar.donation.ui.history.HistoricoAba
import io.solidar.donation.ui.history.HistoricoScreen
import io.solidar.donation.ui.history.HistoricoViewModel
import io.solidar.donation.ui.history.sampleDoacoes
import io.solidar.donation.ui.profile.PerfilScreen
import io.solidar.donation.ui.profile.PerfilViewModel
import io.solidar.donation.ui.profile.samplePerfilPf
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
    onNavigateToEditarPerfil: () -> Unit = {},
    onNavigateToAlterarSenha: () -> Unit = {},
    campanhasViewModel: CampanhasViewModel = viewModel { AppContainer.default.createCampanhasViewModel() },
    historicoViewModel: HistoricoViewModel = viewModel { AppContainer.default.createHistoricoViewModel() },
    perfilViewModel: PerfilViewModel = viewModel { AppContainer.default.createPerfilViewModel() },
) {
    val wide =
        currentWindowAdaptiveInfo()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val campanhasUiState by campanhasViewModel.uiState.collectAsStateWithLifecycle()
    val historicoUiState by historicoViewModel.uiState.collectAsStateWithLifecycle()
    val perfilUiState by perfilViewModel.uiState.collectAsStateWithLifecycle()

    MainNavigationContent(
        wide = wide,
        selectedTab = campanhasUiState.abaPrincipal,
        onTabSelected = campanhasViewModel::selecionarAbaPrincipal,
        historicoTab = historicoUiState.abaSelecionada,
        onHistoricoTabSelected = historicoViewModel::selecionarAba,
        estadoCampanhas = campanhasUiState.estadoLista,
        favoritosIds = campanhasUiState.favoritosIds,
        onRecarregarCampanhas = campanhasViewModel::carregarCampanhas,
        estadoHistorico = historicoUiState.estadoLista,
        onRecarregarHistorico = historicoViewModel::carregarHistorico,
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
    estadoCampanhas: TelaEstado<List<Campaign>> = TelaEstado.Sucesso(sampleCampaigns),
    favoritosIds: Set<String> = emptySet(),
    onRecarregarCampanhas: () -> Unit = {},
    estadoHistorico: TelaEstado<List<Doacao>> = TelaEstado.Sucesso(sampleDoacoes),
    onRecarregarHistorico: () -> Unit = {},
    estadoPerfil: TelaEstado<PerfilUsuario> = TelaEstado.Sucesso(samplePerfilPf),
    mensagemFeedbackPerfil: String? = null,
    onRecarregarPerfil: () -> Unit = {},
    onEditarPerfilClick: () -> Unit = {},
    onAlterarSenhaClick: () -> Unit = {},
) {
    if (wide) {
        Row(modifier = modifier.fillMaxSize()) {
            NavigationRail(
                header = {
                    FloatingActionButton(onClick = onNavigateToCreateDonation) {
                        Icon(AppIcons.Add, contentDescription = "Criar doação")
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
                    historicoTab = historicoTab,
                    onHistoricoTabSelected = onHistoricoTabSelected,
                    estadoCampanhas = estadoCampanhas,
                    favoritosIds = favoritosIds,
                    onRecarregarCampanhas = onRecarregarCampanhas,
                    estadoHistorico = estadoHistorico,
                    onRecarregarHistorico = onRecarregarHistorico,
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
                    onClick = onNavigateToCreateDonation,
                    icon = { Icon(AppIcons.Add, contentDescription = null) },
                    text = { Text("Doar") },
                    modifier =
                        Modifier.semantics {
                            contentDescription = "Criar nova doação"
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
                historicoTab = historicoTab,
                onHistoricoTabSelected = onHistoricoTabSelected,
                estadoCampanhas = estadoCampanhas,
                favoritosIds = favoritosIds,
                onRecarregarCampanhas = onRecarregarCampanhas,
                estadoHistorico = estadoHistorico,
                onRecarregarHistorico = onRecarregarHistorico,
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
    historicoTab: HistoricoAba,
    onHistoricoTabSelected: (HistoricoAba) -> Unit,
    estadoCampanhas: TelaEstado<List<Campaign>>,
    favoritosIds: Set<String>,
    onRecarregarCampanhas: () -> Unit,
    estadoHistorico: TelaEstado<List<Doacao>>,
    onRecarregarHistorico: () -> Unit,
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
                )
            MainTab.FAVORITES -> {
                val campanhasAtuais = (estadoCampanhas as? TelaEstado.Sucesso)?.dados.orEmpty()
                val campanhasFavoritas = campanhasAtuais.filter { favoritosIds.contains(it.id) }

                if (campanhasFavoritas.isEmpty()) {
                    PlaceholderScreen(
                        title = "Nenhuma campanha salva",
                        subtitle = "As campanhas que você marcar nos favoritos aparecerão aqui.",
                        icon = AppIcons.Favorite,
                    )
                } else {
                    HomeScreen(
                        estado = TelaEstado.Sucesso(campanhasFavoritas),
                        onCampaignClick = onCampaignClick,
                        onTentarDeNovo = onRecarregarCampanhas,
                        wide = wide,
                        titulo = "Campanhas favoritas",
                    )
                }
            }
            MainTab.EXPLORE ->
                PlaceholderScreen(
                    title = "Explorar Causas e ONGs",
                    subtitle = "Em breve você poderá buscar doações, categorias e instituições próximas.",
                    icon = AppIcons.Search,
                )
            MainTab.ACTIVITIES ->
                HistoricoScreen(
                    estado = estadoHistorico,
                    selectedTab = historicoTab,
                    onTabSelected = onHistoricoTabSelected,
                    onTentarDeNovo = onRecarregarHistorico,
                )
            MainTab.PROFILE ->
                PerfilScreen(
                    estado = estadoPerfil,
                    wide = wide,
                    mensagemFeedback = mensagemFeedbackPerfil,
                    onEditarPerfilClick = onEditarPerfilClick,
                    onAlterarSenhaClick = onAlterarSenhaClick,
                    onTentarDeNovo = onRecarregarPerfil,
                    onLogout = onLogout,
                )
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
