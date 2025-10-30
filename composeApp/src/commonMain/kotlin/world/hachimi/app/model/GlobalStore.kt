package world.hachimi.app.model

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.*
import world.hachimi.app.api.module.VersionModule
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import world.hachimi.app.nav.Navigator
import world.hachimi.app.nav.Route
import world.hachimi.app.player.Player
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferencesKeys
import world.hachimi.app.storage.SongCache
import kotlin.time.Duration

/**
 * Global shared data and logic. Can work without UI displaying
 */
class GlobalStore(
    private val dataStore: MyDataStore,
    private val api: ApiClient,
    private val innerPlayer: Player,
    songCache: SongCache
) {
    var initialized by mutableStateOf(false)
    var darkMode by mutableStateOf<Boolean?>(null)
        private set
    val nav = Navigator(Route.Root.Home)
    var isLoggedIn by mutableStateOf(false)
        private set
    var userInfo by mutableStateOf<UserInfo?>(null)
        private set
    var playerExpanded by mutableStateOf(false)
        private set
    val player = PlayerService(this, dataStore, api, innerPlayer, songCache)
    private val scope = CoroutineScope(Dispatchers.Default)
    val snackbarHostState = SnackbarHostState()

    @Serializable
    data class MusicQueueItem(
        val id: Long,
        val displayId: String,
        val name: String,
        val artist: String,
        val duration: Duration,
        val coverUrl: String
    )

    fun initialize() = scope.launch {
        scope.launch(Dispatchers.Default) {
            coroutineScope {
                launch { this@GlobalStore.darkMode = dataStore.get(PreferencesKeys.SETTINGS_DARK_MODE) }
                launch { loadLoginStatus() }
            }
            initialized = true
        }
        scope.launch {
            checkMinApiVersion()
        }
        scope.launch {
            checkUpdate()
        }
    }

    fun updateDarkMode(darkMode: Boolean?) = scope.launch {
        this@GlobalStore.darkMode = darkMode
        if (darkMode == null) {
            dataStore.delete(PreferencesKeys.SETTINGS_DARK_MODE)
        } else {
            dataStore.set(PreferencesKeys.SETTINGS_DARK_MODE, darkMode)
        }
    }

    private suspend fun loadLoginStatus() {
        val uid = dataStore.get(PreferencesKeys.USER_UID)
        val username = dataStore.get(PreferencesKeys.USER_NAME)
        val avatar = dataStore.get(PreferencesKeys.USER_AVATAR)
        val accessToken = dataStore.get(PreferencesKeys.AUTH_ACCESS_TOKEN)
        val refreshToken = dataStore.get(PreferencesKeys.AUTH_REFRESH_TOKEN)

        if (uid != null && username != null && accessToken != null && refreshToken != null) {
            api.setToken(accessToken, refreshToken)
            api.setAuthListener(object : AuthenticationListener {
                override suspend fun onTokenChange(accessToken: String, refreshToken: String) {
                    dataStore.set(PreferencesKeys.AUTH_ACCESS_TOKEN, accessToken)
                    dataStore.set(PreferencesKeys.AUTH_REFRESH_TOKEN, refreshToken)
                }

                override suspend fun onAuthenticationError(err: AuthError) {
                    // TODO: Should we process other errors?
                    when (err) {
                        is AuthError.RefreshTokenError -> {
                            logout()
                            alert("登录令牌失效，请重新登录")
                            nav.push(Route.Auth())
                        }
                        is AuthError.ErrorHttpResponse -> {}
                        is AuthError.UnknownError -> {}
                        is AuthError.UnauthorizedDuringRequest -> {}
                    }
                }
            })
            isLoggedIn = true
            userInfo = UserInfo(uid, username, avatarUrl = avatar)
        }
    }

    fun logout() = scope.launch {
        api.setToken(null, null)
        dataStore.delete(PreferencesKeys.USER_UID)
        dataStore.delete(PreferencesKeys.USER_NAME)
        dataStore.delete(PreferencesKeys.USER_AVATAR)
        dataStore.delete(PreferencesKeys.AUTH_ACCESS_TOKEN)
        dataStore.delete(PreferencesKeys.AUTH_REFRESH_TOKEN)
        nav.replace(Route.Root.Home)
        isLoggedIn = false
        userInfo = null
    }

//    @Deprecated("Use alert with i18n instead")
    fun alert(text: String?) {
        scope.launch {
            snackbarHostState.showSnackbar(text?.take(64) ?: "Unknown Error", withDismissAction = true)
        }
    }

    fun alert(text: StringResource, vararg params: Any?) {
        TODO()
    }

    fun expandPlayer() {
        playerExpanded = true
    }

    fun shrinkPlayer() {
        playerExpanded = false
    }

    fun setLoginUser(uid: Long, name: String, avatarUrl: String?, update: Boolean) {
        Snapshot.withMutableSnapshot {
            if (update && userInfo == null) {
                return
            }
            userInfo = UserInfo(
                uid = uid,
                name = name,
                avatarUrl = avatarUrl
            )
            isLoggedIn = true
        }
        scope.launch {
            dataStore.set(PreferencesKeys.USER_NAME, name)
            dataStore.set(PreferencesKeys.USER_UID, uid)

            avatarUrl?.let {
                dataStore.set(PreferencesKeys.USER_AVATAR, avatarUrl)
            }
        }
    }


    var showApiVersionIncompatible by mutableStateOf(false)
        private set
    var serverVersion by mutableStateOf("")
    var serverMinVersion by mutableStateOf("")
    val clientApiVersion by mutableStateOf(ApiClient.VERSION)

    private suspend fun checkMinApiVersion() {
        try {
            val resp = api.versionModule.server()
            // We assume it won't return false
            val data = resp.ok()
            serverVersion = data.version.toString()
            serverMinVersion = data.minVersion.toString()

            if (ApiClient.VERSION < data.minVersion) {
                showApiVersionIncompatible = true
            }
        } catch (e: Throwable) {
            Logger.e("player", "Failed to check min API version", e)
            alert("Failed to check min API version")
        }
    }

    var checkingUpdate by mutableStateOf(false)
        private set
    var showUpdateDialog by mutableStateOf(false)
        private set
    var currentVersion by mutableStateOf(BuildKonfig.VERSION_NAME)
        private set
    var newVersionInfo by mutableStateOf<VersionModule.LatestVersionResp?>(null)
        private set
    private suspend fun checkUpdate() {
        checkingUpdate = true
        try {
            val variant = getPlatform().variant
            val resp = api.versionModule.latest(VersionModule.LatestVersionReq(variant))
            if (resp.ok) {
                val data = resp.ok()
                if (data != null && data.versionNumber > BuildKonfig.VERSION_CODE) {
                    newVersionInfo = data
                    showUpdateDialog = true
                }
            } else {
                alert(resp.err().msg)
            }
        } catch (e: Throwable) {
            Logger.e("global", "Failed to check update", e)
            alert("检查更新失败")
        } finally {
            checkingUpdate = false
        }
    }

    fun dismissUpgrade() {
        // TODO: ignore this version anymore
        showUpdateDialog = false
    }

    fun confirmUpgrade() {
        showUpdateDialog = false
        getPlatform().openUrl(newVersionInfo!!.url)
    }
}