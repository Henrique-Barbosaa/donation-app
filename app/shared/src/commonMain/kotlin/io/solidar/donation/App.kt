package io.solidar.donation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.solidar.donation.theme.DonationTheme
import io.solidar.donation.ui.auth.LoginScreen
import io.solidar.donation.ui.auth.SignUpScreen
import io.solidar.donation.ui.history.HistoricoAba
import io.solidar.donation.ui.history.HistoricoScreen
import io.solidar.donation.ui.history.sampleDoacoes
import io.solidar.donation.ui.home.HomeScreen
import io.solidar.donation.ui.home.sampleCampaigns

enum class Screen {
    LOGIN,
    SIGN_UP,
    HOME,
    HISTORICO,
}

@Composable
@Preview
fun App() {
    DonationTheme {
        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
        var historicoTab by remember { mutableStateOf(HistoricoAba.FEITAS) }

        when (currentScreen) {
            Screen.LOGIN -> {
                LoginScreen(
                    onNavigateToSignUp = { currentScreen = Screen.SIGN_UP },
                    onLoginSuccess = { currentScreen = Screen.HOME },
                )
            }
            Screen.SIGN_UP -> {
                SignUpScreen(
                    onNavigateBack = { currentScreen = Screen.LOGIN },
                    onSignUpSuccess = { currentScreen = Screen.HOME },
                )
            }
            Screen.HOME -> {
                Column {
                    // TODO: trocar por um componente de navegação de verdade quando existir.
                    Text(
                        text = "Ver histórico",
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable { currentScreen = Screen.HISTORICO }
                    )
                    HomeScreen(
                        campaigns = sampleCampaigns,
                        onCampaignClick = { /* TODO: navegar para o detalhe da campanha */ }
                    )
                }
            }
            Screen.HISTORICO -> {
                HistoricoScreen(
                    doacoes = sampleDoacoes,
                    selectedTab = historicoTab,
                    onTabSelected = { historicoTab = it }
                )
            }
        }
    }
}