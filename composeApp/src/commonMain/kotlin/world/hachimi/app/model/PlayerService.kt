package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readBuffer
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import world.hachimi.app.model.GlobalStore.MusicQueueItem
import world.hachimi.app.player.PlayEvent
import world.hachimi.app.player.Player
import world.hachimi.app.player.SongItem
import world.hachimi.app.storage.SongCache
import kotlin.time.Duration.Companion.seconds

private val downloadHttpClient = HttpClient() {
    install(HttpTimeout) {
        connectTimeoutMillis = 60_000
        requestTimeoutMillis = 60_000
        socketTimeoutMillis = 60_000
    }
}

class PlayerService(
    private val global: GlobalStore,
    private val api: ApiClient,
    private val player: Player,
    private val songCache: SongCache
) {
    val playerState = PlayerUIState()
    val musicQueue = mutableStateListOf<MusicQueueItem>()
    var queueCurrentIndex by mutableStateOf(-1)
    private val queueMutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch(Dispatchers.Default) {
            player.addListener(object : Player.Listener {
                override fun onEvent(event: PlayEvent) {
                    when (event) {
                        PlayEvent.End -> {
                            playerState.isPlaying = false
                            queueNext()
                        }

                        is PlayEvent.Error -> {
                            global.alert(event.e.message)
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
                if (player.isPlaying()) {
                    val currentPosition = player.currentPosition()
                    playerState.updateCurrentMillis(currentPosition)
                }
                delay(100)
            }
        }
    }

    /**
     * This is only for play current song. Not interested in the music queue.
     */
    private suspend fun play(displayId: String) = coroutineScope {
        player.pause()
        try {
            playerState.fetchingMetadata = true
            playerState.downloadProgress = 0f

            val item = getSongItemCacheable(
                displayId = displayId,
                onMetadata = { songInfo ->
                    playerState.updateSongInfo(songInfo)
                    playerState.hasSong = true
                    playerState.updateCurrentMillis(0L)
                },
                onProgress = { progress ->
                    playerState.buffering = true
                    playerState.downloadProgress = progress
                }
            )

            player.prepare(item, autoPlay = true)

            launch {
                try {
                    val songId = item.id.toLong()
                    // Touch playing
                    if (global.isLoggedIn) {
                        api.playHistoryModule.touch(songId)
                    } else {
                        api.playHistoryModule.touchAnonymous(songId)
                    }
                } catch (e: Throwable) {
                    Logger.e("player", "Failed to touch song", e)
                }
            }
        } catch (e: Throwable) {
            Logger.e("player", "Failed to play song", e)
            global.alert(e.message)
            return@coroutineScope
        } finally {
            playerState.fetchingMetadata = false
            playerState.buffering = false
        }
    }


    fun playSongInQueue(id: String) = scope.launch {
        queueMutex.withLock {
            val index = musicQueue.indexOfFirst { it.displayId == id }
            if (index != -1) {
                val song = musicQueue[index]

                queueCurrentIndex = index
                play(song.displayId)
            }
        }
    }

    // TODO[refactor](player): Should queue be a builtin feature in player? To make GlobalStore more clear
    fun queuePrevious() = scope.launch {
        queueMutex.withLock {
            if (musicQueue.isNotEmpty()) {
                val currentIndex = musicQueue.indexOfFirst { it.displayId == playerState.songDisplayId }
                if (currentIndex == -1) {
                    playSongInQueue(musicQueue.first().displayId)
                } else {
                    if (currentIndex == 0) {
                        playSongInQueue(musicQueue.last().displayId)
                    } else {
                        playSongInQueue(musicQueue[currentIndex - 1].displayId)
                    }
                }
            }
        }
    }

    fun queueNext() = scope.launch {
        queueMutex.withLock {
            if (musicQueue.isNotEmpty()) {
                val currentIndex = musicQueue.indexOfFirst { it.displayId == playerState.songDisplayId }
                if (currentIndex == -1) {
                    playSongInQueue(musicQueue.first().displayId)
                } else {
                    if (currentIndex >= musicQueue.lastIndex) {
                        playSongInQueue(musicQueue.first().displayId)
                    } else {
                        playSongInQueue(musicQueue[currentIndex + 1].displayId)
                    }
                }
            }
        }
    }

    /**
     * Add song to queue
     * @param instantPlay instantly play after inserting
     * @param append appends to tail or insert after current playing
     */
    fun insertToQueue(songDisplayId: String, instantPlay: Boolean, append: Boolean) = scope.launch {
        queueMutex.withLock {
            player.pause()
            playerState.fetchingMetadata = true
            val indexInQueue = musicQueue.indexOfFirst { it.displayId == songDisplayId }

            // Remove and reinsert
            if (indexInQueue != -1) {
                musicQueue.removeAt(indexInQueue)
            }

            val currentPlayingIndex = musicQueue.indexOfFirst { it.displayId == playerState.songDisplayId }
            try {
                val cache = songCache.getMetadata(songDisplayId)

                if (cache != null) {
                    Logger.i("global", "Cache hit")
                }

                val data = cache ?: run {
                    val resp = api.songModule.detail(songDisplayId)
                    if (resp.ok) {
                        val data = resp.ok<SongModule.PublicSongDetail>()
                        songCache.saveMetadata(data)
                        data
                    } else {
                        val err = resp.errData<CommonError>()
                        global.alert(err.msg)
                        return@launch
                    }
                }


                val item = MusicQueueItem(
                    id = data.id,
                    displayId = data.displayId,
                    name = data.title,
                    artist = data.uploaderName,
                    duration = data.durationSeconds.seconds,
                    coverUrl = data.coverUrl
                )

                if (append) {
                    musicQueue.add(currentPlayingIndex + 1, item)
                } else {
                    musicQueue.add(item)
                }

                if (instantPlay) {
                    playerState.hasSong = true
                    queueNext()
                }
            } catch (e: Throwable) {
                Logger.e("player", "Failed to insert song to queue", e)
                global.alert(e.message)
            } finally {
                playerState.fetchingMetadata = false
            }
        }
    }

    fun insertToQueue(item: MusicQueueItem, instantPlay: Boolean, append: Boolean) = scope.launch {

    }

    fun playAll(items: List<MusicQueueItem>) {
        scope.launch {
            queueMutex.withLock {
                player.pause()
                musicQueue.clear()
                musicQueue.addAll(items)
                queueCurrentIndex = -1
                queueNext()
            }
        }
    }

    fun removeFromQueue(id: String) = scope.launch {
        queueMutex.withLock {
            val currentPlayingIndex = musicQueue.indexOfFirst { it.displayId == playerState.songDisplayId }
            val targetIndex = musicQueue.indexOfFirst { it.displayId == id }

            if (currentPlayingIndex == targetIndex) {
                if (musicQueue.size > 1) {
                    queueNext()
                } else {
                    player.pause()
                    playerState.hasSong = false
                }
            }
            musicQueue.removeAt(targetIndex)
        }
    }

    fun playOrPause() = scope.launch {
        if (player.isPlaying()) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun setSongProgress(progress: Float) = scope.launch {
        val millis = (progress * (playerState.songDurationSecs * 1000L)).toLong()
        player.seek(millis, true)

        // Update UI instantly
        // FIXME(player): This might be overwrite by progress syncing job
        playerState.updateCurrentMillis(millis)
    }

    private suspend fun getSongItemCacheable(
        displayId: String,
        onMetadata: (SongDetailInfo) -> Unit,
        onProgress: (Float) -> Unit
    ): SongItem = coroutineScope {
        val cache = songCache.get(displayId)
        val metadata: SongDetailInfo
        val coverBytes: ByteArray
        val audioBytes: ByteArray

        if (cache != null) {
            Logger.i("global", "Cache hit")
            val buffer = Buffer()
            metadata = cache.metadata
            onMetadata(cache.metadata)
            onProgress(1f)
            cache.audio.use {
                it.transferTo(buffer)
            }
            coverBytes = cache.cover.readByteArray()
            audioBytes = buffer.readByteArray()
        } else {
            Logger.i("global", "Downloading")
            val data = songCache.getMetadata(displayId) ?: run {
                val resp = api.songModule.detail(displayId)
                if (!resp.ok) error(resp.errData<CommonError>().msg)
                resp.ok()
            }

            metadata = data
            onMetadata(data)

            val coverBytesAsync = async<ByteArray>(Dispatchers.Default) {
                api.httpClient.get(data.coverUrl).bodyAsBytes()
            }

            // Do not use HttpCache plugin because it will affect the progress (Bugs)
            val statement = downloadHttpClient.prepareGet(data.audioUrl)

            // FIXME(wasm)(player): Due to the bugs of ktor client, we can't get the content length header in wasm target
            //  KTOR-8377 JS/WASM: response doesn't contain the Content-Length header in a browser
            //  https://youtrack.jetbrains.com/issue/KTOR-8377/JS-WASM-response-doesnt-contain-the-Content-Length-header-in-a-browser
            //  KTOR-7934 JS/WASM fails with "IllegalStateException: Content-Length mismatch" on requesting gzipped content
            //  https://youtrack.jetbrains.com/issue/KTOR-7934/JS-WASM-fails-with-IllegalStateException-Content-Length-mismatch-on-requesting-gzipped-content
            var headContentLength: Long? = null
            // Workaround for wasm, we can use HEAD request to get the content length
            if (getPlatform().name == "wasm") {
                val resp = api.httpClient.head(data.audioUrl)
                headContentLength = resp.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: 0L
                Logger.i("global", "Head content length: $headContentLength bytes")
            }
            val buffer = statement.execute { resp ->
                val contentLength = resp.headers[HttpHeaders.ContentLength]?.toLongOrNull()
                Logger.i("global", "Content length: $contentLength bytes")

                val bestContentLength = contentLength ?: headContentLength
                val channel = resp.body<ByteReadChannel>()

                val buffer = if (bestContentLength != null) {
                    val buffer = Buffer()
                    var count = 0L
                    while (!channel.exhausted()) {
                        val chunk = channel.readRemaining(1024 * 8)
                        count += chunk.transferTo(buffer)
                        val progress = count.toFloat() / bestContentLength
                        onProgress(progress.coerceIn(0f, 1f))
                    }
                    buffer
                } else {
                    Logger.i("global", "Content-Length not found, progress is disabled")
                    channel.readBuffer()
                }

                buffer
            }
            coverBytes = coverBytesAsync.await()
            val cacheItem = SongCache.Item(
                key = displayId,
                metadata = data,
                audio = buffer.copy(),
                cover = Buffer().also { it.write(coverBytes) }
            )
            audioBytes = buffer.readByteArray()
            songCache.save(cacheItem)
        }

        val filename = metadata.audioUrl.substringAfterLast("/")
        val extension = filename.substringAfterLast(".")
        val item = SongItem(
            id = metadata.id.toString(),
            title = metadata.title,
            artist = metadata.uploaderName,
            audioBytes = audioBytes,
            coverBytes = coverBytes,
            format = extension
        )
        return@coroutineScope item
    }
}