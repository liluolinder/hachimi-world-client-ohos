package world.hachimi.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Route.Auth
import world.hachimi.app.nav.Route.Root
import world.hachimi.app.ui.auth.AuthScreen
import world.hachimi.app.ui.root.RootScreen

@Composable
fun App() {
    val rootDestination = GlobalStore.nav.backStack.last()

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box {
                when(rootDestination) {
                    is Root -> RootScreen(rootDestination.child)
                    is Auth -> AuthScreen(rootDestination.initialLogin)
                }
                FloatingActionButton(onClick = {
                    GlobalStore.nav.back()
                }) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        }
    }
}