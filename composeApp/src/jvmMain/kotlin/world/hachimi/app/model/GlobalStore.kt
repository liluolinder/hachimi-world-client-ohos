package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.AuthError
import world.hachimi.app.api.AuthenticationListener
import world.hachimi.app.nav.Route
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.RootContent
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferencesKeys

class GlobalStore(
    private val dataStore: MyDataStore,
    private val apiClient: ApiClient
) {
    var initialized by mutableStateOf(false)
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

    private val scope = CoroutineScope(Dispatchers.Default)

    fun initialize() {
        scope.launch {
            coroutineScope {
                launch { loadLoginStatus() }
            }
            initialized = true
        }
    }

    private suspend fun loadLoginStatus() {
        val uid = dataStore.get(PreferencesKeys.USER_UID)
        val username = dataStore.get(PreferencesKeys.USER_NAME)
        val accessToken = dataStore.get(PreferencesKeys.AUTH_ACCESS_TOKEN)
        val refreshToken = dataStore.get(PreferencesKeys.AUTH_REFRESH_TOKEN)

        if (uid != null && username != null && accessToken != null && refreshToken != null) {
            apiClient.setToken(accessToken, refreshToken)
            apiClient.setAuthListener(object : AuthenticationListener {
                override suspend fun onTokenChange(accessToken: String, refreshToken: String) {
                    dataStore.set(PreferencesKeys.AUTH_ACCESS_TOKEN, accessToken)
                    dataStore.set(PreferencesKeys.AUTH_REFRESH_TOKEN, refreshToken)
                }

                override suspend fun onAuthenticationError(err: AuthError) {
                    // TODO: Ask user to re-login
                    println(err)
                    when (err) {
                        is AuthError.ErrorHttpResponse -> {}
                        is AuthError.RefreshTokenError -> {}
                        is AuthError.UnknownError -> {}
                        is AuthError.UnauthorizedDuringRequest -> {}
                    }
                }
            })
            isLoggedIn = true
            userInfo = UserInfo(uid, username, avatarUrl = null)
        }
    }

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