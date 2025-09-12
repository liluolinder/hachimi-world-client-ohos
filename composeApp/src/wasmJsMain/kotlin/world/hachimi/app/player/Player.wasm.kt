package world.hachimi.app.player

class WasmPlayer : Player {
    override suspend fun isPlaying(): Boolean {
        // TODO:
        return false
    }

    override suspend fun isEnd(): Boolean {
        // TODO:
        return false
    }

    override suspend fun currentPosition(): Long {
        // TODO:
        return 0
    }

    override suspend fun play() {
        // TODO:
    }

    override suspend fun pause() {
        // TODO:
    }

    override suspend fun seek(position: Long, autoStart: Boolean) {
        // TODO:
    }

    override suspend fun getVolume(): Float {
        // TODO:
        return 1f
    }

    override suspend fun setVolume(value: Float) {
        // TODO:
    }

    override suspend fun prepare(item: SongItem, autoPlay: Boolean) {
        // TODO:
    }

    override suspend fun isReady(): Boolean {
        // TODO:
        return true
    }

    override suspend fun release() {
        // TODO:
    }

    override fun addListener(listener: Player.Listener) {
        // TODO:
    }

    override fun removeListener(listener: Player.Listener) {
        // TODO:
    }
}