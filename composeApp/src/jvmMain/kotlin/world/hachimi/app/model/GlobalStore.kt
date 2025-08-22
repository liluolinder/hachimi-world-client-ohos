package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import org.jetbrains.compose.resources.StringResource
import world.hachimi.app.nav.Route
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.RootContent

object GlobalStore {
    val nav = Navigator(Route.Root(RootContent.Home))
    var isLoggedIn by mutableStateOf(false)
        private set
    var userInfo by mutableStateOf<UserInfo?>(null)
        private set
    var playerExpanded by mutableStateOf(false)
        private set
    var currentSong by mutableStateOf("")
        private set
    val playerInfo = PlayerInfo()

    @Deprecated("Use alert with i18n instead")
    fun alert(text: String) {

    }

    fun alert(text: StringResource, vararg params: Any?) {

    }

    fun setCurrentSong() {

    }

    fun expandPlayer() {
        playerExpanded = true
    }

    fun shrinkPlayer() {
        playerExpanded = false
    }

    fun setLoginUser(uid: Long, name: String, avatarUrl: String?) {
        Snapshot.withMutableSnapshot {
            userInfo = UserInfo(
                uid = uid,
                name = name,
                avatarUrl = avatarUrl
            )
            isLoggedIn = true
        }
    }
}

class PlayerInfo() {
    var isPlaying by mutableStateOf(false)
    var songDuration by mutableStateOf(0)
    var currentSongDuration by mutableStateOf(0)
    var currentLyricsLine by mutableStateOf(0)
}

data class UserInfo(
    val uid: Long,
    val name: String,
    val avatarUrl: String? = null
)