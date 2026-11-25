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
import io.solidar.donation.navigation.CriarCampanhaRoute
import io.solidar.donation.navigation.CriarDoacaoRoute
import io.solidar.donation.navigation.DEEP_LINK_CAMPANHA
import io.solidar.donation.navigation.DoarDinheiroRoute
import io.solidar.donation.navigation.DoarItensCampanhaRoute
import io.solidar.donation.navigation.EfetivarDoacaoRoute
import io.solidar.donation.navigation.LoginRoute
import io.solidar.donation.navigation.MainRoute
import io.solidar.donation.navigation.OpcoesDoacaoRoute
import io.solidar.donation.navigation.SignUpRoute
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.auth.LoginScreen
import io.solidar.donation.ui.auth.SignUpScreen
import io.solidar.donation.ui.campaign.CampanhaDetalheScreen
import io.solidar.donation.ui.campaign.CriarCampanhaScreen
import io.solidar.donation.ui.donation.CriarDoacaoScreen
import io.solidar.donation.ui.donation.DoarDinheiroScreen
import io.solidar.donation.ui.donation.DoarItensCampanhaScreen
import io.solidar.donation.ui.donation.EfetivarDoacaoScreen
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
    val explorarViewModel = viewModel { appContainer.createExplorarViewModel() }

    NavHost(navController = navController, startDestination = AuthGraph) {
        navigation<AuthGraph>(startDestination = LoginRoute) {
            composable<LoginRoute> {
                LoginScreen(
                    onNavigateToSignUp = { navController.navigate(SignUpRoute) },
                    onLoginSuccess = {
                        val ehInst = appContainer.authRepository.getActiveSession()?.tipo == "INSTITUICAO"
                        campanhasViewModel.carregarCampanhas()
                        historicoViewModel.carregarHistorico(carregarCampanhas = ehInst)
                        explorarViewModel.carregarDoacoes()
                        navController.irParaMain()
                    },
                    viewModel = authViewModel,
                )
            }
            composable<SignUpRoute> {
                SignUpScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSignUpSuccess = {
                        val ehInst = appContainer.authRepository.getActiveSession()?.tipo == "INSTITUICAO"
                        campanhasViewModel.carregarCampanhas()
                        historicoViewModel.carregarHistorico(carregarCampanhas = ehInst)
                        explorarViewModel.carregarDoacoes()
                        navController.irParaMain()
                    },
                    viewModel = authViewModel,
                )
            }
        }

        composable<MainRoute> {
            val activeSession = appContainer.authRepository.getActiveSession()
            MainNavigationScaffold(
                campanhasViewModel = campanhasViewModel,
                historicoViewModel = historicoViewModel,
                explorarViewModel = explorarViewModel,
                activeSession = activeSession,
                onNavigateToCreateDonation = { navController.navigate(CriarDoacaoRoute) },
                onNavigateToCreateCampaign =
                    if (activeSession?.tipo == "INSTITUICAO") {
                        { navController.navigate(CriarCampanhaRoute) }
                    } else {
                        null
                    },
                onEfetivarDoacao = { doacao ->
                    navController.navigate(EfetivarDoacaoRoute(id = doacao.id, itemTitle = doacao.itemTitle))
                },
                onCampaignClick = { navController.navigate(CampanhaDetalheRoute(it.id)) },
                onLogout = {
                    authViewModel.logout()
                    campanhasViewModel.limparEstadoSessao()
                    historicoViewModel.limparEstadoSessao()
                    explorarViewModel.limparEstadoSessao()
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
                    explorarViewModel.carregarDoacoes()
                    navController.popBackStack()
                },
            )
        }

        composable<CriarCampanhaRoute> {
            val criarCampanhaViewModel = viewModel { appContainer.createCriarCampanhaViewModel() }
            CriarCampanhaScreen(
                viewModel = criarCampanhaViewModel,
                onNavigateBack = { navController.popBackStack() },
                onCampaignCreated = {
                    campanhasViewModel.carregarCampanhas()
                    historicoViewModel.carregarHistorico(carregarCampanhas = true)
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
            val activeSession = appContainer.authRepository.getActiveSession()
            val ehOrganizador = activeSession?.id != null && campaign?.instituicaoId == activeSession.id

            CampanhaDetalheScreen(
                campaign = campaign,
                isFavorito = campanhasUiState.favoritosIds.contains(route.id),
                onToggleFavorito = { campanhasViewModel.alternarFavorito(route.id) },
                ehOrganizador = ehOrganizador,
                onFinalizarCampanha =
                    if (ehOrganizador) {
                        {
                            campanhasViewModel.finalizarCampanha(route.id) {
                                historicoViewModel.carregarHistorico(carregarCampanhas = true)
                                navController.popBackStack()
                            }
                        }
                    } else {
                        null
                    },
                onCancelarCampanha =
                    if (ehOrganizador) {
                        {
                            campanhasViewModel.cancelarCampanha(route.id) {
                                historicoViewModel.carregarHistorico(carregarCampanhas = true)
                                navController.popBackStack()
                            }
                        }
                    } else {
                        null
                    },
                cancelandoCampanha = campanhasUiState.cancelandoCampanha,
                erroCancelamento = campanhasUiState.erroCancelamento,
                onBack = {
                    campanhasViewModel.limparErroCancelamento()
                    if (!navController.popBackStack()) navController.navigate(AuthGraph)
                },
                onQueroDoar = {
                    if (campaign != null && !ehOrganizador) {
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
                    onDoarDinheiro = { navController.navigate(DoarDinheiroRoute(campaign.id)) },
                    onDoarItens = { navController.navigate(DoarItensCampanhaRoute(campaign.id)) },
                )
            }
        }

        composable<DoarItensCampanhaRoute> { entry ->
            val route = entry.toRoute<DoarItensCampanhaRoute>()
            val campaign = campanhasViewModel.buscarCampanhaPorId(route.id)
            val doarItensViewModel = viewModel { appContainer.createDoarItensCampanhaViewModel() }

            if (campaign != null) {
                DoarItensCampanhaScreen(
                    campaign = campaign,
                    viewModel = doarItensViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onDoacaoConcluida = {
                        historicoViewModel.carregarHistorico()
                        explorarViewModel.carregarDoacoes()
                        navController.popBackStack()
                    },
                )
            }
        }

        composable<DoarDinheiroRoute> { entry ->
            val route = entry.toRoute<DoarDinheiroRoute>()
            val campaign = campanhasViewModel.buscarCampanhaPorId(route.id)
            val doarDinheiroViewModel = viewModel { appContainer.createDoarDinheiroViewModel() }

            if (campaign != null) {
                DoarDinheiroScreen(
                    campaign = campaign,
                    viewModel = doarDinheiroViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onContribuicaoConcluida = {
                        historicoViewModel.carregarHistorico()
                        navController.popBackStack()
                    },
                )
            }
        }

        composable<EfetivarDoacaoRoute> { entry ->
            val route = entry.toRoute<EfetivarDoacaoRoute>()
            val efetivarViewModel = viewModel { appContainer.createEfetivarDoacaoViewModel() }

            EfetivarDoacaoScreen(
                doacaoId = route.id,
                itemTitle = route.itemTitle,
                viewModel = efetivarViewModel,
                onNavigateBack = { navController.popBackStack() },
                onEfetivadaComSucesso = {
                    historicoViewModel.carregarHistorico()
                    explorarViewModel.carregarDoacoes()
                    navController.popBackStack()
                },
            )
        }
    }
}

private fun NavHostController.irParaMain() {
    navigate(MainRoute) {
        popUpTo<AuthGraph> { inclusive = true }
    }
}
