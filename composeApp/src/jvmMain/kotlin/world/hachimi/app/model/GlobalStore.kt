package world.hachimi.app.model

import androidx.compose.runtime.mutableStateOf
import world.hachimi.app.nav.Route
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.RootContent

object GlobalStore {
    val nav = Navigator(Route.Root(RootContent.Home))
    val isLoggedIn = mutableStateOf(false)
    val userInfo = mutableStateOf<UserInfo?>(null)
}

data class UserInfo(
    val name: String,
    val avatarUrl: String? = null
)