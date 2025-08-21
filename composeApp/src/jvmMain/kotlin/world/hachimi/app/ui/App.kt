package world.hachimi.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.home.HomeScreen
import world.hachimi.app.ui.login.LoginScreen

@Composable
fun App() {
    val rootDestination = GlobalStore.nav.backStack.first()

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when(rootDestination) {
                "home" -> HomeScreen()
                "login" -> LoginScreen()
            }
        }
    }
}