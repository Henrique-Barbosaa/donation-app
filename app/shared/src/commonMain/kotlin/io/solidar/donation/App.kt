package io.solidar.donation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.auth.LoginScreen
import io.solidar.donation.ui.auth.SignUpScreen
import io.solidar.donation.ui.history.HistoricoAba
import io.solidar.donation.ui.history.HistoricoScreen
import io.solidar.donation.ui.history.sampleDoacoes
import io.solidar.donation.ui.home.CampanhaDetalheScreen
import io.solidar.donation.ui.home.HomeScreen
import io.solidar.donation.ui.home.sampleCampaigns
import kotlinx.serialization.Serializable

@Serializable object Login

@Serializable object Cadastro

@Serializable object Home

@Serializable object Historico

@Serializable data class CampanhaDetalhe(val id: String)

@Composable
fun App() {
    DonationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val nav = rememberNavController()
            var aba by remember { mutableStateOf(HistoricoAba.FEITAS) }

            NavHost(nav, startDestination = Login) {
                composable<Login> {
                    LoginScreen(
                        onNavigateToSignUp = { nav.navigate(Cadastro) },
                        onLoginSuccess = {
                            nav.navigate(Home) { popUpTo<Login> { inclusive = true } }
                        },
                    )
                }
                composable<Cadastro> {
                    SignUpScreen(
                        onNavigateBack = { nav.popBackStack() },
                        onSignUpSuccess = {
                            nav.navigate(Home) { popUpTo<Login> { inclusive = true } }
                        },
                    )
                }
                composable<Home> {
                    HomeScreen(
                        campaigns = sampleCampaigns,
                        onCampaignClick = { nav.navigate(CampanhaDetalhe(it.id)) },
                        onVerHistorico = { nav.navigate(Historico) },
                    )
                }
                composable<CampanhaDetalhe>(
                    deepLinks = listOf(navDeepLink<CampanhaDetalhe>(basePath = "solidar://campanha")),
                ) { entry ->
                    val rota = entry.toRoute<CampanhaDetalhe>()
                    CampanhaDetalheScreen(
                        campaign = sampleCampaigns.find { it.id == rota.id },
                        onVoltar = { nav.popBackStack() },
                    )
                }
                composable<Historico> {
                    HistoricoScreen(
                        doacoes = sampleDoacoes,
                        selectedTab = aba,
                        onTabSelected = { aba = it },
                    )
                }
            }
        }
    }
}
