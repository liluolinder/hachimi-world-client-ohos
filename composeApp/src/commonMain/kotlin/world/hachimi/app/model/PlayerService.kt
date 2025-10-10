package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
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
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private val downloadHttpClient = HttpClient() {
    install(HttpTimeout) {
        connectTimeoutMillis = 60_000
        requestTimeoutMillis = 60_000
        socketTimeoutMillis = 60_000
    }
}

/**
 * TODO(player): There are too fucking many racing conditions here. I think I should totally rewrite this.
 */
class PlayerService(
    private val global: GlobalStore,
    private val api: ApiClient,
    private val player: Player,
    private val songCache: SongCache
) {
    val playerState = PlayerUIState()
    var musicQueue by mutableStateOf<List<MusicQueueItem>>(emptyList())
        private set

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

    private var playerMutex = Mutex()
    private var playerJobSign: Int = 0

    /**
     * This is only for play current song. Not interested in the music queue.
     */
    private suspend fun play(item: MusicQueueItem) = coroutineScope {
        player.pause()
        val sign = Random.nextInt()

        playerMutex.withLock { playerJobSign = sign }

        try {
            playerState.downloadProgress = 0f
            playerState.updatePreviewMetadata(PlayerUIState.PreviewMetadata(
                id = item.id,
                displayId = item.displayId,
                title = item.name,
                author = item.artist,
                coverUrl = item.coverUrl,
                duration = item.duration
            ))
            playerState.fetchingMetadata = true

            val item = getSongItemCacheable(
                displayId = item.displayId,
                onMetadata = { songInfo ->
                    playerMutex.withLock {
                        if (playerJobSign == sign) {
                            playerState.updateSongInfo(songInfo)
                            playerState.hasSong = true
                            playerState.updateCurrentMillis(0L)
                        }
                    }
                },
                onProgress = { progress ->
                    playerMutex.withLock {
                        if (playerJobSign == sign) {
                            playerState.buffering = true
                            playerState.downloadProgress = progress
                        }
                    }
                }
            )

            playerMutex.withLock {
                if (playerJobSign == sign) {
                    player.prepare(item, autoPlay = true)
                }
            }

            // Touch in the global scope
            scope.launch {
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
        } catch (_: CancellationException) {
            Logger.i("player", "Preparing cancelled")
        } catch (e: Throwable) {
            Logger.e("player", "Failed to play song", e)
            global.alert(e.message)
        } finally {
            playerMutex.withLock {
                if (playerJobSign == sign) {
                    playerState.fetchingMetadata = false
                    playerState.buffering = false
                }
            }
        }
    }


    private var playPrepareJob: Job? = null

    fun playSongInQueue(id: Long) = scope.launch {
        if (playPrepareJob?.isActive == true) {
            // We don't use cancelAndJoin because we want the operation to be instant
            Logger.d("player", "Cancel prepare job")
            playPrepareJob?.cancel()
        }
        val song = queueMutex.withLock {
            val index = musicQueue.indexOfFirst { it.id == id }
            if (index != -1) {
                musicQueue[index]
            } else {
                null
            }
        }
        if (song != null) {
            playPrepareJob = scope.launch {
                Logger.d("playSongInQueue", "Playing song $song")
                play(song)
            }
        }
    }

    // TODO[refactor](player): Should queue be a builtin feature in player? To make GlobalStore more clear
    fun queuePrevious() = scope.launch {
        val targetSong = queueMutex.withLock {
            if (musicQueue.isNotEmpty()) {
                val currentSongId = if (playerState.fetchingMetadata) playerState.previewMetadata?.id else playerState.songInfo?.id

                val currentIndex = musicQueue.indexOfFirst {
                    it.id == currentSongId
                }
                val targetIdx = when {
                    currentIndex == -1 -> 0 // First
                    currentIndex == 0 -> musicQueue.lastIndex // Ring
                    else -> currentIndex - 1 // Previous
                }
                val targetSong = musicQueue[targetIdx]
                return@withLock targetSong
            }
            return@withLock null
        }
        targetSong?.let {
            playSongInQueue(it.id)
        }
    }

    fun queueNext() = scope.launch {
        val targetSong = queueMutex.withLock {
            if (musicQueue.isNotEmpty()) {
                // Get current song index (including the fetching/buffering)
                val currentSongId = if (playerState.fetchingMetadata) playerState.previewMetadata?.id else playerState.songInfo?.id

                val currentIndex = musicQueue.indexOfFirst {
                    it.id == currentSongId
                }
                val targetIdx = when {
                    currentIndex == -1 -> 0 // First
                    currentIndex >= musicQueue.lastIndex -> 0 // Ring
                    else -> currentIndex + 1 // Next
                }
                val targetSong = musicQueue[targetIdx]
                return@withLock targetSong
            }
            return@withLock null
        }

        targetSong?.let {
            playSongInQueue(it.id)
        }
    }

    private var fetchMetadataJob: Job? = null

    fun insertToQueueWithFetch(
        songDisplayId: String,
        instantPlay: Boolean,
        append: Boolean
    ) {
        if (fetchMetadataJob?.isActive == true) {
            fetchMetadataJob?.cancel()
        }

        fetchMetadataJob = scope.launch {
            playerState.fetchingMetadata = true
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

                insertToQueue(item, instantPlay, append).join()
            } catch (e: CancellationException) {
                // Do nothing, it's just cancelled
                Logger.i("player", "Cancelled")
            } catch (e: Throwable) {
                global.alert(e.message)
                Logger.e("global", "Failed to insert song to music queue", e)
            } finally {
                fetchMetadataJob = null
                playerState.fetchingMetadata = false
            }
        }
    }

    /**
     * Add song to queue
     * @param instantPlay instantly play after inserting
     * @param append appends to tail or insert after current playing
     */
    fun insertToQueue(
        item: MusicQueueItem,
        instantPlay: Boolean,
        append: Boolean
    ) = scope.launch {
        player.pause()

        queueMutex.withLock {
            val indexInQueue = musicQueue.indexOfFirst { it.id == item.id }

            if (indexInQueue != -1) {
                // If the music was already in the queue, just play it
                // Or we can reorder it after
            } else {
                // Add to queue
                val currentPlayingIndex = musicQueue.indexOfFirst { it.id == playerState.songInfo?.id }

                val queue = musicQueue.toMutableList()

                if (append) {
                    queue.add(item)
                } else {
                    queue.add(currentPlayingIndex + 1, item)
                }
                musicQueue = queue
            }
        }

        if (instantPlay) {
            playerState.hasSong = true
            playSongInQueue(item.id)
        }
    }

    fun playAll(items: List<MusicQueueItem>) = scope.launch {
        queueMutex.withLock {
            player.pause()
            musicQueue = items
            queueNext()
        }
    }

    fun removeFromQueue(id: Long) = scope.launch {
        queueMutex.withLock {
            val currentPlayingIndex = musicQueue.indexOfFirst { it.id == playerState.songInfo?.id }
            val targetIndex = musicQueue.indexOfFirst { it.id == id }

            if (currentPlayingIndex == targetIndex) {
                if (musicQueue.size > 1) {
                    queueNext()
                } else {
                    player.pause()
                    playerState.hasSong = false
                }
            }
            musicQueue = musicQueue.toMutableList().apply {
                removeAt(targetIndex)
            }.toList()
        }
    }

    fun playOrPause() = scope.launch {
        // TODO: Redownload, if the song download failed
        if (!playerState.fetchingMetadata && !playerState.buffering) {
            if (player.isPlaying()) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun setSongProgress(progress: Float) = scope.launch {
        if (!playerState.fetchingMetadata && !playerState.buffering) {
            playerState.songInfo?.let { songInfo ->
                val millis = (progress * (songInfo.durationSeconds * 1000L)).toLong()
                player.seek(millis, true)

                // Update UI instantly
                // FIXME(player): This might be overwrite by progress syncing job
                playerState.updateCurrentMillis(millis)
            }
        }
    }

    fun updateVolume(volume: Float) = scope.launch {
        playerState.volume = volume
        player.setVolume(volume)
    }

    private suspend fun getSongItemCacheable(
        displayId: String,
        onMetadata: suspend (SongDetailInfo) -> Unit,
        onProgress: suspend (Float) -> Unit
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
            onProgress(0f)
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