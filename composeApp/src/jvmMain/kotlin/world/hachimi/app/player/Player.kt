package world.hachimi.app.player

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.hachimi.app.logging.Logger
import java.io.ByteArrayInputStream
import javax.sound.sampled.*

/**
 * A player can work without UI
 */
interface Player {
    fun isPlaying(): Boolean
    fun isEnd(): Boolean
    fun currentPosition(): Long

    fun play()
    fun pause()
    fun seek(position: Long, autoStart: Boolean = false)

    fun getVolume(): Float
    fun setVolume(value: Float)

    /**
     * Download from URL and prepare to play
     * Might throw Exception
     */
    suspend fun prepare(bytes: ByteArray, autoPlay: Boolean = false)
    fun isReady(): Boolean

    fun release()
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)

    interface Listener {
        fun onEvent(event: PlayEvent)
    }
}

sealed class PlayEvent {
    object Play : PlayEvent()
    object Pause : PlayEvent()
    object End : PlayEvent()

    /**
     * Might happen during playing while downloading
     */
    data class Error(val e: Exception) : PlayEvent()
    data class Seek(val position: Long) : PlayEvent()
}

class PlayerImpl() : Player {
    private var clip: Clip = AudioSystem.getLine(DataLine.Info(Clip::class.java, null)) as Clip
    private var volumeControl: FloatControl? = null

    private lateinit var stream: AudioInputStream
    private var ready = false

    override suspend fun prepare(bytes: ByteArray, autoPlay: Boolean): Unit = withContext(Dispatchers.IO) {
        if (isPlaying()) {
            clip.stop()
            clip.close()
        }

        ready = false
        clip = AudioSystem.getLine(DataLine.Info(Clip::class.java, null)) as Clip
        val defaultFormat = clip.format

        val stream = AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes))
        val baseFormat = stream.format
//        val sampleSizeInBites = baseFormat.sampleSizeInBits.takeIf { it > 0 } ?: 32 // Defaults to fltp(32bits)

        val decodedStream = AudioSystem.getAudioInputStream(defaultFormat, stream)

        clip.open(decodedStream)
        if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
            volumeControl = clip.getControl(FloatControl.Type.VOLUME) as FloatControl
        } else {
            volumeControl = null
        }
        ready = true

        if (autoPlay) {
            play()
        }
    }

    override fun isReady(): Boolean {
        return ready
    }

    override fun isPlaying(): Boolean {
        return clip.isRunning
    }

    override fun isEnd(): Boolean {
        return clip.framePosition >= clip.frameLength - 1
    }

    override fun currentPosition(): Long {
        return clip.microsecondPosition / 1000L
    }

    override fun play() {
        if (ready) {
            clip.start()
        }
    }

    override fun pause() {
        if (ready) {
            clip.stop()
        }
    }

    override fun seek(position: Long, autoStart: Boolean) {
        if (autoStart || isPlaying()) {
            clip.stop()
            clip.microsecondPosition = position * 1000L
            clip.start()
        } else {
            clip.microsecondPosition = position * 1000L
        }
    }

    override fun getVolume(): Float {
        return volumeControl?.value ?: 1f
    }

    override fun setVolume(value: Float) {
        volumeControl?.value = value
    }

    override fun release() {
        // Do some cleanup work
        try {
            clip.stop()
            clip.close()
            stream.close()
        } catch (e: Exception) {
            Logger.e("PlayerImpl", "release: Error closing resources", e)
        }
    }

    override fun addListener(listener: Player.Listener) {
        TODO()
    }

    override fun removeListener(listener: Player.Listener) {
        TODO()
    }

    suspend fun drain() = withContext(Dispatchers.IO) {
        clip.drain()
    }
}