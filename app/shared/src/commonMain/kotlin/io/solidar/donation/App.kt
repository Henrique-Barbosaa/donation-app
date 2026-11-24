package io.solidar.donation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import io.solidar.donation.di.AppContainer
import io.solidar.donation.navigation.AuthGraph
import io.solidar.donation.navigation.CampanhaDetalheRoute
import io.solidar.donation.navigation.CriarDoacaoRoute
import io.solidar.donation.navigation.DEEP_LINK_CAMPANHA
import io.solidar.donation.navigation.LoginRoute
import io.solidar.donation.navigation.MainRoute
import io.solidar.donation.navigation.OpcoesDoacaoRoute
import io.solidar.donation.navigation.SignUpRoute
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.auth.LoginScreen
import io.solidar.donation.ui.auth.SignUpScreen
import io.solidar.donation.ui.campaign.CampanhaDetalheScreen
import io.solidar.donation.ui.donation.CriarDoacaoScreen
import io.solidar.donation.ui.donation.OpcoesDoacaoScreen
import io.solidar.donation.ui.home.CampanhasViewModel
import io.solidar.donation.ui.home.MainNavigationScaffold

@Composable
@Preview
fun App(
    navController: NavHostController = rememberNavController(),
    appContainer: AppContainer = AppContainer.default,
) {
    DonationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            DonationNavHost(
                navController = navController,
                appContainer = appContainer,
            )
        }
    }
}

@Composable
fun DonationNavHost(
    navController: NavHostController,
    appContainer: AppContainer = AppContainer.default,
) {
    val authViewModel = viewModel { appContainer.createAuthViewModel() }
    val campanhasViewModel: CampanhasViewModel = viewModel { appContainer.createCampanhasViewModel() }
    val historicoViewModel = viewModel { appContainer.createHistoricoViewModel() }

    NavHost(navController = navController, startDestination = AuthGraph) {
        navigation<AuthGraph>(startDestination = LoginRoute) {
            composable<LoginRoute> {
                LoginScreen(
                    onNavigateToSignUp = { navController.navigate(SignUpRoute) },
                    onLoginSuccess = { navController.irParaMain() },
                    viewModel = authViewModel,
                )
            }
            composable<SignUpRoute> {
                SignUpScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSignUpSuccess = { navController.irParaMain() },
                    viewModel = authViewModel,
                )
            }
        }

        composable<MainRoute> {
            MainNavigationScaffold(
                campanhasViewModel = campanhasViewModel,
                historicoViewModel = historicoViewModel,
                onNavigateToCreateDonation = { navController.navigate(CriarDoacaoRoute) },
                onCampaignClick = { navController.navigate(CampanhaDetalheRoute(it.id)) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AuthGraph) {
                        popUpTo<MainRoute> { inclusive = true }
                    }
                },
            )
        }

        composable<CriarDoacaoRoute> {
            val criarDoacaoViewModel = viewModel { appContainer.createCriarDoacaoViewModel() }
            CriarDoacaoScreen(
                viewModel = criarDoacaoViewModel,
                onNavigateBack = { navController.popBackStack() },
                onDonationCreated = {
                    historicoViewModel.carregarHistorico()
                    navController.popBackStack()
                },
            )
        }

        composable<CampanhaDetalheRoute>(
            deepLinks = listOf(navDeepLink<CampanhaDetalheRoute>(basePath = DEEP_LINK_CAMPANHA)),
        ) { entry ->
            val route = entry.toRoute<CampanhaDetalheRoute>()
            val campanhasUiState by campanhasViewModel.uiState.collectAsStateWithLifecycle()
            val campaign = campanhasViewModel.buscarCampanhaPorId(route.id)

            CampanhaDetalheScreen(
                campaign = campaign,
                isFavorito = campanhasUiState.favoritosIds.contains(route.id),
                onToggleFavorito = { campanhasViewModel.alternarFavorito(route.id) },
                onBack = {
                    if (!navController.popBackStack()) navController.navigate(AuthGraph)
                },
                onQueroDoar = {
                    if (campaign != null) {
                        navController.navigate(OpcoesDoacaoRoute(campaign.id))
                    }
                },
            )
        }

        composable<OpcoesDoacaoRoute> { entry ->
            val route = entry.toRoute<OpcoesDoacaoRoute>()
            val campaign = campanhasViewModel.buscarCampanhaPorId(route.id)

            if (campaign != null) {
                OpcoesDoacaoScreen(
                    campaign = campaign,
                    onBack = { navController.popBackStack() },
                    onDoarDinheiro = { /* TODO: Rota do PIX */ },
                    onDoarItens = { /* TODO: Rota do Formulário */ },
                )
            }
        }
    }
}

private fun NavHostController.irParaMain() {
    navigate(MainRoute) {
        popUpTo<AuthGraph> { inclusive = true }
    }
}
