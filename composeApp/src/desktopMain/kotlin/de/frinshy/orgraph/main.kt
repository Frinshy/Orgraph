package de.frinshy.orgraph

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowStyle
import de.frinshy.orgraph.presentation.viewmodel.OrgraphViewModel

fun main() = application {
    val windowState = rememberWindowState(
        position = WindowPosition(Alignment.Center),
        size = DpSize(1280.dp, 720.dp)
    )

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Orgraph - School Teacher Management",
        resizable = true,
        icon = BitmapPainter(useResource("images/icon.png", ::loadImageBitmap))
    ) {
        val viewModel: OrgraphViewModel = viewModel()
        val isDarkTheme by viewModel.isDarkTheme.collectAsState()
        
        WindowStyle(
            isDarkTheme = isDarkTheme,
            backdropType = WindowBackdrop.Mica
        )

        App()
    }
}