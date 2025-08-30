package world.hachimi.app.player

class AndroidPlayer: Player {
    override fun isPlaying(): Boolean {
        // TODO:
        return false
    }

    override fun isEnd(): Boolean {
        // TODO:
        return false
    }

    override fun currentPosition(): Long {
        // TODO:
        return 0
    }

    override fun play() {
        // TODO:
    }

    override fun pause() {
        // TODO:
    }

    override fun seek(position: Long, autoStart: Boolean) {
        // TODO:
    }

    override fun getVolume(): Float {
        // TODO:
        return 1f
    }

    override fun setVolume(value: Float) {
        // TODO:
    }

    override suspend fun prepare(bytes: ByteArray, autoPlay: Boolean) {
        // TODO:
    }

    override fun isReady(): Boolean {
        // TODO:
        return false
    }

    override fun release() {
        // TODO:
    }

    override fun addListener(listener: Player.Listener) {
        // TODO:
    }

    override fun removeListener(listener: Player.Listener) {
        // TODO:
    }
}