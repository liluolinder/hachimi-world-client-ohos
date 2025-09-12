package world.hachimi.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.koin.core.context.startKoin
import world.hachimi.app.di.appModule
import world.hachimi.app.font.WithFont
import world.hachimi.app.ui.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(appModule)
    }

    ComposeViewport(document.body!!) {
        LaunchedEffect(Unit) {
            document.querySelector("#loading")?.remove()
        }
        WithFont {
            App()
        }
    }
}
