package org.donation.app

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import org.donation.app.theme.DonationTheme
import org.donation.app.ui.auth.LoginScreen
import org.donation.app.ui.auth.SignUpScreen

enum class Screen {
    LOGIN, SIGN_UP, HOME
}

@Composable
@Preview
fun App() {
    DonationTheme {
        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

        when (currentScreen) {
            Screen.LOGIN -> {
                LoginScreen(
                    onNavigateToSignUp = { currentScreen = Screen.SIGN_UP },
                    onLoginSuccess = { currentScreen = Screen.HOME }
                )
            }
            Screen.SIGN_UP -> {
                SignUpScreen(
                    onNavigateBack = { currentScreen = Screen.LOGIN },
                    onSignUpSuccess = { currentScreen = Screen.HOME }
                )
            }
            Screen.HOME -> {
                androidx.compose.material3.Text("Home Screen (Em breve)")
            }
        }
    }
}