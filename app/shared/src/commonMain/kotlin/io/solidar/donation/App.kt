package io.solidar.donation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import io.solidar.donation.navigation.AuthGraph
import io.solidar.donation.navigation.CampanhaDetalheRoute
import io.solidar.donation.navigation.CriarDoacaoRoute
import io.solidar.donation.navigation.DEEP_LINK_CAMPANHA
import io.solidar.donation.navigation.LoginRoute
import io.solidar.donation.navigation.MainRoute
import io.solidar.donation.navigation.SignUpRoute
import io.solidar.donation.navigation.OpcoesDoacaoRoute // <-- Importa a tua nova rota
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.auth.LoginScreen
import io.solidar.donation.ui.auth.SignUpScreen
import io.solidar.donation.ui.campaign.CampanhaDetalheScreen
import io.solidar.donation.ui.donation.CriarDoacaoScreen
import io.solidar.donation.ui.donation.OpcoesDoacaoScreen // <-- Importa o ecrã novo
import io.solidar.donation.ui.home.MainNavigationScaffold
import io.solidar.donation.ui.home.sampleCampaigns

@Composable
@Preview
fun App(navController: NavHostController = rememberNavController()) {
    DonationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            DonationNavHost(navController)
        }
    }
}

@Composable
fun DonationNavHost(navController: NavHostController) {
    val campaigns = sampleCampaigns

    NavHost(navController = navController, startDestination = AuthGraph) {
        navigation<AuthGraph>(startDestination = LoginRoute) {
            composable<LoginRoute> {
                LoginScreen(
                    onNavigateToSignUp = { navController.navigate(SignUpRoute) },
                    onLoginSuccess = { navController.irParaMain() },
                )
            }
            composable<SignUpRoute> {
                SignUpScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSignUpSuccess = { navController.irParaMain() },
                )
            }
        }

        composable<MainRoute> {
            MainNavigationScaffold(
                campaigns = campaigns,
                onNavigateToCreateDonation = { navController.navigate(CriarDoacaoRoute) },
                onCampaignClick = { navController.navigate(CampanhaDetalheRoute(it.id)) },
                onLogout = {
                    navController.navigate(AuthGraph) {
                        popUpTo<MainRoute> { inclusive = true }
                    }
                },
            )
        }

        composable<CriarDoacaoRoute> {
            CriarDoacaoScreen(
                onNavigateBack = { navController.popBackStack() },
                onDonationCreated = { navController.popBackStack() },
            )
        }

        composable<CampanhaDetalheRoute>(
            deepLinks = listOf(navDeepLink<CampanhaDetalheRoute>(basePath = DEEP_LINK_CAMPANHA)),
        ) { entry ->
            val route = entry.toRoute<CampanhaDetalheRoute>()
            val campaign = campaigns.firstOrNull { it.id == route.id }

            CampanhaDetalheScreen(
                campaign = campaign,
                onBack = {
                    if (!navController.popBackStack()) navController.navigate(AuthGraph)
                },
                // Passamos o evento para navegar para a tela de opções:
                onQueroDoar = {
                    if (campaign != null) {
                        navController.navigate(OpcoesDoacaoRoute(campaign.id))
                    }
                }
            )
        }

        // NOVO ECRÃ AQUI:
        composable<OpcoesDoacaoRoute> { entry ->
            val route = entry.toRoute<OpcoesDoacaoRoute>()
            val campaign = campaigns.firstOrNull { it.id == route.id }

            if (campaign != null) {
                OpcoesDoacaoScreen(
                    campaign = campaign,
                    onBack = { navController.popBackStack() },
                    onDoarDinheiro = { /* TODO: Rota do PIX */ },
                    onDoarItens = { /* TODO: Rota do Formulário */ }
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
