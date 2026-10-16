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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import io.solidar.donation.model.Campaign
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.components.AppIcons
import io.solidar.donation.ui.components.PlaceholderScreen
import io.solidar.donation.ui.history.HistoricoAba
import io.solidar.donation.ui.history.HistoricoScreen
import io.solidar.donation.ui.history.sampleDoacoes

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
 * Shell principal pós-login. Lê a classe de largura da janela e delega para
 * MainNavigationContent, que recebe a decisão de layout como parâmetro
 * (`wide`) — assim os dois modos podem ser conferidos em preview e testados.
 */
@Composable
fun MainNavigationScaffold(
    onNavigateToCreateDonation: () -> Unit,
    onLogout: () -> Unit,
    onCampaignClick: (Campaign) -> Unit,
    modifier: Modifier = Modifier,
    campaigns: List<Campaign> = sampleCampaigns,
) {
    val wide =
        currentWindowAdaptiveInfo()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var historicoTab by rememberSaveable { mutableStateOf(HistoricoAba.FEITAS) }

    MainNavigationContent(
        wide = wide,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        historicoTab = historicoTab,
        onHistoricoTabSelected = { historicoTab = it },
        campaigns = campaigns,
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
    campaigns: List<Campaign>,
    onCampaignClick: (Campaign) -> Unit,
    onNavigateToCreateDonation: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
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
                    campaigns = campaigns,
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
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { onTabSelected(tab) },
                            // O rótulo visível já nomeia o item para o leitor de tela.
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
                campaigns = campaigns,
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
    campaigns: List<Campaign>,
    onCampaignClick: (Campaign) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Crossfade(targetState = tab, modifier = modifier) { current ->
        when (current) {
            MainTab.HOME ->
                HomeScreen(
                    campaigns = campaigns,
                    onCampaignClick = onCampaignClick,
                    wide = wide,
                )
            MainTab.FAVORITES -> {
                // Filtramos a lista para mostrar apenas as favoritadas
                val campanhasFavoritas = campaigns.filter {
                    io.solidar.donation.util.FavoritosManager.isFavorito(it.id)
                }

                if (campanhasFavoritas.isEmpty()) {
                    PlaceholderScreen(
                        title = "Nenhuma campanha salva",
                        subtitle = "As campanhas que você marcar com ❤️ aparecerão aqui.",
                        icon = AppIcons.Search,
                    )
                } else {
                    // Reutilizamos a tua HomeScreen maravilhosa, mas só com as favoritas!
                    HomeScreen(
                        campaigns = campanhasFavoritas,
                        onCampaignClick = onCampaignClick,
                        wide = wide,
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
                    doacoes = sampleDoacoes,
                    selectedTab = historicoTab,
                    onTabSelected = onHistoricoTabSelected,
                )
            MainTab.PROFILE ->
                PlaceholderScreen(
                    title = "Seu Perfil",
                    subtitle = "Gerencie seus dados, preferências e informações de doador/ONG.",
                    icon = AppIcons.Person,
                    actionButtonText = "Sair da conta",
                    onActionClick = onLogout,
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
            campaigns = sampleCampaigns,
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
            campaigns = sampleCampaigns,
            onCampaignClick = {},
            onNavigateToCreateDonation = {},
            onLogout = {},
        )
    }
}
