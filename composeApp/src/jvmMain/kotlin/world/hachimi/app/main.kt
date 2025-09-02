package world.hachimi.app

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.icon_vector
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import world.hachimi.app.di.appModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.App
import java.awt.Dimension

fun main() {
    System.setProperty("apple.awt.application.appearance", "system")
    startKoin {
        modules(appModule)
    }

    application {
        val global = koinInject<GlobalStore>()
        LaunchedEffect(global) {
            global.initialize()
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Hachimi World",
            state = WindowState(
                size = DpSize(1200.dp, 800.dp)
            ),
            icon = painterResource(Res.drawable.icon_vector)
        ) {
            LaunchedEffect(Unit) {
                window.minimumSize = Dimension(360, 700)
            }
            if (global.initialized) {
                CompositionLocalProvider(LocalDensity provides LocalDensity.current.let {
                    // TODO: Remove this later
                    Density(it.density * 0.9f, it.fontScale)
                }) {
                    App()
                }
            } else {
                // TODO: Add splash screen
                Box() {

                }
            }
        }
    }
}

