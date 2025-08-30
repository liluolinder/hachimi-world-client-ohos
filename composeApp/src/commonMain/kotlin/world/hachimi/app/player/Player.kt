package world.hachimi.app.player

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

