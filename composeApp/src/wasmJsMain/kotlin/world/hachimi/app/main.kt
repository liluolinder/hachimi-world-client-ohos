package world.hachimi.app

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import org.koin.core.context.startKoin
import world.hachimi.app.di.appModule
import world.hachimi.app.font.WithFont
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.App

@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main() {
    val koin = startKoin {
        modules(appModule)
    }

    val global = koin.koin.get<GlobalStore>()
    global.initialize()

    window.addEventListener("popstate") {
        it.preventDefault()
        global.nav.back()
    }

    if (window.location.hash.isEmpty()) {
        window.history.pushState(null, "", "#/")
    }

    val previousBackStack = mutableStateOf(global.nav.backStack.toList())
    val nav = global.nav
    ComposeViewport(document.body!!) {
        LaunchedEffect(Unit) {
            document.querySelector("#loading")?.remove()
        }
        LaunchedEffect(Unit) {
            snapshotFlow { nav.backStack.toList() }
                .collect { newBackStack ->
                    val lastEntry = newBackStack.last()
                    if (previousBackStack.value.size < newBackStack.size) {
                        window.history.pushState(lastEntry.toString().toJsString(), "", "#/${lastEntry}")
                    } else if (previousBackStack.value.size == newBackStack.size) {
                        window.history.replaceState(lastEntry.toString().toJsString(), "", "#/${lastEntry}")
                    } else if (previousBackStack.value.size > newBackStack.size) {

                    }
                    previousBackStack.value = newBackStack
                }
        }
        WithFont {
            if (global.initialized) {
                App()
            } else {
                // TODO: Add splash screen
                Box {}
            }
        }
    }
}
