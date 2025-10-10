package world.hachimi.app.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import world.hachimi.app.logging.Logger
import java.io.ByteArrayInputStream
import javax.sound.sampled.*
import kotlin.math.roundToInt


class JVMPlayer() : Player {
    private var clip: Clip = AudioSystem.getLine(DataLine.Info(Clip::class.java, null)) as Clip
    private var volumeControl: FloatControl? = null
    private var masterGainControl: FloatControl? = null

    private lateinit var stream: AudioInputStream
    private var ready = false
    private val listeners: MutableSet<Player.Listener> = mutableSetOf()

    @get:Synchronized
    @set:Synchronized
    private var pauseByUser = false

    private val mutex = Mutex()

    suspend fun prepare(uri: String, autoPlay: Boolean) {
        /*val bytes = withContext(Dispatchers.IO) {
            val uri = URI.create(uri).toURL()
            uri.readBytes()
        }
        prepare(bytes, autoPlay)*/
    }

    override suspend fun prepare(item: SongItem, autoPlay: Boolean): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            clip.close()

            ready = false
            val stream = withContext(Dispatchers.IO) {
                AudioSystem.getAudioInputStream(ByteArrayInputStream(item.audioBytes))
            }
            val originalFormat = stream.format

            Logger.i("player", "originalFormat = $originalFormat")

            val desiredPcmFormat = AudioFormat( // 16bit, signed-int PCM, with original sampleRate
                AudioFormat.Encoding.PCM_SIGNED,
                originalFormat.sampleRate,
                16,
                originalFormat.channels, // Always be 2(stereo)
                originalFormat.channels * (16 / 8),
                originalFormat.sampleRate, // frameRate is the same as sampleRate
                false
            )
            val clip = AudioSystem.getLine(DataLine.Info(Clip::class.java, desiredPcmFormat)) as Clip
            val defaultFormat = clip.format
            Logger.i("player", "defaultFormat = $defaultFormat")

            val decodedStream = withContext(Dispatchers.IO) {
                AudioSystem.getAudioInputStream(desiredPcmFormat, stream)
            }
            Logger.i("player", "decodedFormat = ${decodedStream.format}")

            clip.addLineListener {
                when (it.type) {
                    LineEvent.Type.START -> listeners.forEach { listener -> listener.onEvent(PlayEvent.Play) }
                    LineEvent.Type.STOP -> {
                        if (pauseByUser) {
                            Logger.i("player", "Pause")
                            pauseByUser = false
                            listeners.forEach { listener -> listener.onEvent(PlayEvent.Pause) }
                        } else {
                            Logger.i("player", "End")
                            listeners.forEach { listener -> listener.onEvent(PlayEvent.End) }
                        }
                    }
                }
            }
            clip.open(decodedStream)

            val previousVolume = getVolume()
            volumeControl = if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                clip.getControl(FloatControl.Type.VOLUME) as FloatControl
            } else null
            masterGainControl = if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            } else null

            setVolume(previousVolume)
            Logger.i("player", "volumeControl = $volumeControl")
            Logger.i("player", "masterGainControl = $masterGainControl")

            ready = true
            this@JVMPlayer.clip = clip

            if (autoPlay) {
                play()
            }
        }
    }

    override suspend fun isReady(): Boolean {
        return ready
    }

    override suspend fun isPlaying(): Boolean {
        return clip.isRunning
    }

    override suspend fun isEnd(): Boolean {
        return clip.framePosition >= clip.frameLength - 1
    }

    override suspend fun currentPosition(): Long {
        return clip.microsecondPosition / 1000L
    }

    override suspend fun play() {
        if (ready) {
            clip.start()
        }
    }

    override suspend fun pause() {
        if (ready) {
            pauseByUser = true
            clip.stop()
        }
    }

    override suspend fun seek(position: Long, autoStart: Boolean) {
        if (autoStart || isPlaying()) {
            pauseByUser = true
            clip.stop()
            clip.microsecondPosition = position * 1000L
            clip.start()
        } else {
            clip.microsecondPosition = position * 1000L
        }
    }

    override suspend fun getVolume(): Float {
        return if (volumeControl != null) {
            volumeControl?.value ?: 1f
        } else if (masterGainControl != null) {
            masterGainControl?.let {
//                (it.value - it.minimum) / (it.maximum - it.minimum)
                // The maximum gain could be +6 DB, should we make it available to users?
                (it.value - it.minimum) / (0 - it.minimum)
            } ?: 1f
        } else {
            1f
        }
    }

    override suspend fun setVolume(value: Float) {
        if (volumeControl != null) {
            volumeControl?.value = value
        } else if (masterGainControl != null) {
            masterGainControl?.let {
//                val db = ((it.maximum - it.minimum) * value + it.minimum).roundToInt().toFloat()
                val db = ((0 - it.minimum) * value + it.minimum).roundToInt().toFloat()
                masterGainControl?.value = db
                Logger.d("player", "Set master gain: $db db")
            }
        }
    }

    override suspend fun release() {
        // Do some cleanup work
        try {
            clip.stop()
            clip.close()
            stream.close()
        } catch (e: Throwable) {
            Logger.e("PlayerImpl", "release: Error closing resources", e)
        }
    }

    override fun addListener(listener: Player.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
    }

    suspend fun drain() = withContext(Dispatchers.IO) {
        clip.drain()
    }
}