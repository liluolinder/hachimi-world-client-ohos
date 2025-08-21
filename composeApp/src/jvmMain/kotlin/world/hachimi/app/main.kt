package world.hachimi.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import world.hachimi.app.di.appModule
import world.hachimi.app.ui.App
import java.awt.Dimension

fun main() = application {
    startKoin {
        modules(appModule)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Hachimi World",
        state = WindowState(
            size = DpSize(1200.dp, 800.dp)
        )
    ) {
        LaunchedEffect(Unit) {
            window.minimumSize = Dimension(1000, 700)
        }
        App()
    }
}