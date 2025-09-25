package de.frinshy.orgraph

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowStyle

fun main() = application {
    val windowState = rememberWindowState(
        position = WindowPosition(Alignment.Center),
        size = DpSize(1280.dp, 720.dp)
    )

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Orgraph - School Teacher Management",
        resizable = true
    ) {
        WindowStyle(
            isDarkTheme = isSystemInDarkTheme(),
            backdropType = WindowBackdrop.Mica
        )

        App()
    }
}