package world.hachimi.app.player

/**
 * A player can work without UI
 */
interface Player {
    suspend fun isPlaying(): Boolean
    suspend fun isEnd(): Boolean
    suspend fun currentPosition(): Long

    suspend fun play()
    suspend fun pause()
    suspend fun seek(position: Long, autoStart: Boolean = false)

    suspend fun getVolume(): Float
    suspend fun setVolume(value: Float)

    /**
     * Download from URL and prepare to play
     * Might throw Exception
     */
    suspend fun prepare(bytes: ByteArray, autoPlay: Boolean = false)
    suspend fun isReady(): Boolean

    suspend fun release()
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

