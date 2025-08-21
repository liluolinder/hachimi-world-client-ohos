package world.hachimi.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import world.hachimi.app.font.WithFont

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        LaunchedEffect(Unit) {
            document.querySelector("#loading")?.remove()
        }
        WithFont {
            App()
        }
    }
}
