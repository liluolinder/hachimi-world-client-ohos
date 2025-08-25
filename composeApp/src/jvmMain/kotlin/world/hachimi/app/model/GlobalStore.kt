package world.hachimi.app.model

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import org.jetbrains.compose.resources.StringResource
import world.hachimi.app.JVMPlatform
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.AuthError
import world.hachimi.app.api.AuthenticationListener
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.logging.Logger
import world.hachimi.app.nav.Route
import world.hachimi.app.nav.Navigator
import world.hachimi.app.player.PlayEvent
import world.hachimi.app.player.Player
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferencesKeys
import world.hachimi.app.util.LrcParser

/**
 * Global shared data and logic. Can work without UI displaying
 */
class GlobalStore(
    private val dataStore: MyDataStore,
    private val apiClient: ApiClient,
    private val player: Player
) {
    var initialized by mutableStateOf(false)
    val nav = Navigator(Route.Root.Home)
    var isLoggedIn by mutableStateOf(false)
        private set
    var userInfo by mutableStateOf<UserInfo?>(null)
        private set
    var playerExpanded by mutableStateOf(false)
        private set

    val playerState = PlayerUIState()

    private val scope = CoroutineScope(Dispatchers.IO)
    val snackbarHostState = SnackbarHostState()

    fun initialize() {
        scope.launch(Dispatchers.IO) {
            coroutineScope {
                launch { loadLoginStatus() }
            }
            initialized = true
        }
        scope.launch(Dispatchers.IO) {
            player.addListener(object : Player.Listener {
                override fun onEvent(event: PlayEvent) {
                    when (event) {
                        PlayEvent.End -> {
                            playerState.isPlaying = false
                        }

                        is PlayEvent.Error -> {

                        }

                        PlayEvent.Pause -> {
                            playerState.isPlaying = false
                        }

                        PlayEvent.Play -> {
                            playerState.isPlaying = true
                        }

                        is PlayEvent.Seek -> {

                        }
                    }
                }

            })
            while (isActive) {
                val currentPosition = player.currentPosition()
                playerState.setCurrentSongPosition(currentPosition)
                delay(100)
            }
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
        scope.launch {
            snackbarHostState.showSnackbar(text, withDismissAction = true)
        }
    }

    fun alert(text: StringResource, vararg params: Any?) {

    }

    fun setCurrentSong() {

    }

    fun playSong(id: String) {
        // TODO: Extract this logic
        scope.launch(Dispatchers.IO) {
            player.pause()

            playerState.isLoading = true
            try {
                val resp = apiClient.songModule.detail(id)
                if (resp.ok) {
                    Logger.i("global", "Reading detail")
                    val data = resp.okData<SongModule.DetailResp>()
                    Snapshot.withMutableSnapshot {
                        playerState.songId = id
                        playerState.hasSong = true
                        playerState.songCoverUrl = data.coverUrl
                        playerState.songTitle = data.title
                        playerState.songAuthor = data.uploaderUid.toString() // TODO: Be user name
                        playerState.songDurationSecs = data.durationSeconds
                        playerState.setLyrics(data.lyrics)
                    }
                    playerState.setCurrentSongPosition(0L)

                    val filename = data.audioUrl.substringAfterLast("/")
                    val cacheFile = JVMPlatform.getCacheDir()
                        .resolve("song_caches").also {
                            it.mkdirs()
                        }
                        .resolve(filename)
                    val bytes = if (cacheFile.exists()) {
                        cacheFile.readBytes()
                    } else {
                        Logger.i("global", "Downloading")
                        playerState.downloadProgress = 0f
                        val downloadResponse = apiClient.httpClient.get(data.audioUrl)

                        val songBytes: ByteArray
                        val contentLength = downloadResponse.headers[HttpHeaders.ContentLength]?.toLongOrNull()
                        if (contentLength != null) {
                            Logger.i("global", "Has content length")
                            playerState.downloadProgress = 0.01f

                            val buffer = Buffer()
                            val channel = downloadResponse.bodyAsChannel()
                            var totalBytesRead = 0L

                            while (isActive) {
                                val byteBuffer = ByteArray(4096)
                                val bytesRead = channel.readAvailable(byteBuffer, 0, byteBuffer.size)

                                if (bytesRead == -1) break

                                totalBytesRead += bytesRead
                                buffer.write(byteBuffer, 0, bytesRead)
                                val progress = totalBytesRead.toDouble() / contentLength.toDouble()
                                playerState.downloadProgress = progress.toFloat()
                            }

                            songBytes = buffer.readBytes()
                        } else {
                            // Oh, copy occurs here
                            songBytes = downloadResponse.bodyAsBytes()
                        }

                        cacheFile.writeBytes(songBytes)
                        songBytes
                    }
                    player.prepare(bytes, autoPlay = true)
                } else {
                    alert(resp.errData<CommonError>().msg)
                    return@launch
                }
            } catch (e: Exception) {
                Logger.e("player", "Failed to play song", e)
                alert(e.localizedMessage)
                return@launch
            } finally {
                playerState.isLoading = false
            }
        }
    }

    fun playOrPause() {
        if (player.isPlaying()) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun setSongProgress(progress: Float) {
        val targetPositionMs = (progress * (playerState.songDurationSecs * 1000L)).toLong()
        player.seek(targetPositionMs, true)

        playerState.setCurrentSongPosition(targetPositionMs)
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

/**
 * UI states, should attach a player
 */
class PlayerUIState() {
    var hasSong by mutableStateOf(false)
    var songId by mutableStateOf("")
    var isPlaying by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var songTitle by mutableStateOf("")
    var songAuthor by mutableStateOf("")
    var songCoverUrl by mutableStateOf<String?>(null)
    var songDurationSecs by mutableStateOf(0)

    var currentSongPositionMs by mutableStateOf(0L)
        private set
    var currentLyricsLine by mutableStateOf(-1)
        private set
    var timedLyricsEnabled by mutableStateOf(false)
        private set
    var lyricsLines by mutableStateOf<List<String>>(emptyList())
        private set
    private var lrcSegments: List<TimedLyricsSegment> = emptyList()

    var downloadProgress by mutableStateOf(0f)

    data class TimedLyricsSegment(
        val startTimeMs: Long,
        val endTimeMs: Long,
        val spans: List<TimedLyricsSpan>
    )

    data class TimedLyricsSpan(
        val startTimeMs: Long,
        val endTimeMs: Long,
        val text: String
    )

    fun setCurrentSongPosition(milliseconds: Long) {
        currentSongPositionMs = milliseconds

        if (timedLyricsEnabled) {
            val currentLineIndex = lrcSegments.indexOfFirst {
                it.startTimeMs <= milliseconds && milliseconds <= it.endTimeMs
            }
            currentLyricsLine = currentLineIndex
        }
    }

    fun setLyrics(content: String) {
        try {
            val lrcLines = LrcParser.parse(content)
            val result = mutableListOf<TimedLyricsSegment>()
            for ((index, line) in lrcLines.withIndex()) {
                val startTime = line.timestampMs

                val next = lrcLines.getOrNull(index + 1)
                val endTime = next?.timestampMs ?: Long.MAX_VALUE

                val segment = TimedLyricsSegment(
                    startTimeMs = startTime,
                    endTimeMs = endTime,
                    // TODO: Support enhanced lrc later
                    spans = listOf(TimedLyricsSpan(startTime, endTime, line.content))
                )
                result.add(segment)
            }
            this.lrcSegments = result
            lyricsLines = lrcSegments.map { it.spans.first().text }
            timedLyricsEnabled = true
        } catch (e: Exception) {
            lyricsLines = content.lines()
            timedLyricsEnabled = false
        }
    }
}

data class UserInfo(
    val uid: Long,
    val name: String,
    val avatarUrl: String? = null
)