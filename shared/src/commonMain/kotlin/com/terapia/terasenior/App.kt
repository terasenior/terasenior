package com.terapia.terasenior

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.terapia.terasenior.models.Profile
import com.terapia.terasenior.treatment.ui.NumberSearchGame
import com.terapia.terasenior.treatment.ui.TreatmentMenuScreen
import com.terapia.terasenior.ui.login.LoginScreen
import com.terapia.terasenior.ui.theme.TeraseniorTheme

enum class Screen {
    MENU, NUMBER_SEARCH
}

@Composable
@Preview
fun App() {
    TeraseniorTheme {
        // Estado para guardar el perfil del usuario autenticado
        var currentUserProfile by remember { mutableStateOf<Profile?>(null) }
        var currentScreen by remember { mutableStateOf(Screen.MENU) }

        if (currentUserProfile == null) {
            // 1. Si no se ha iniciado sesión, mostramos la pantalla de Login
            LoginScreen { profile ->
                currentUserProfile = profile
            }
        } else {
            // 2. Una vez autenticado, accede al menú y a los ejercicios/juegos
            when (currentScreen) {
                Screen.MENU -> TreatmentMenuScreen(
                    onNumberSearchClick = { currentScreen = Screen.NUMBER_SEARCH }
                )
                Screen.NUMBER_SEARCH -> NumberSearchGame(
                    onBack = { currentScreen = Screen.MENU }
                )
            }
        }
    }
}