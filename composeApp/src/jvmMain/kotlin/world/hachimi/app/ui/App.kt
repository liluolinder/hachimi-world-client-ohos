package world.hachimi.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Route.Auth
import world.hachimi.app.nav.Route.Root
import world.hachimi.app.ui.auth.AuthScreen
import world.hachimi.app.ui.player.PlayerScreen
import world.hachimi.app.ui.root.RootScreen
import world.hachimi.app.ui.theme.AppTheme

@Composable
fun App() {
    val global = koinInject<GlobalStore>()
    val rootDestination = global.nav.backStack.last()

    AppTheme(darkTheme = false) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize()) {
                when(rootDestination) {
                    is Root -> RootScreen(rootDestination)
                    is Auth -> AuthScreen(rootDestination.initialLogin)
                }
                /*FloatingActionButton(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    onClick = { global.nav.back() }
                ) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }*/

                AnimatedVisibility(visible = global.playerExpanded, modifier = Modifier.fillMaxSize()) {
                    PlayerScreen()
                }

                SnackbarHost(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp),
                    hostState = global.snackbarHostState,
                )
            }
        }
    }
}