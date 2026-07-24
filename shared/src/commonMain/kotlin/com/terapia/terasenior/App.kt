package com.terapia.terasenior

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import com.terapia.terasenior.treatment.ui.NumberSearchGame
import com.terapia.terasenior.treatment.ui.TreatmentMenuScreen
import org.jetbrains.compose.resources.painterResource
import terasenior.shared.generated.resources.Res
import terasenior.shared.generated.resources.compose_multiplatform

enum class Screen {
    MENU, NUMBER_SEARCH
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(Screen.MENU) }

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