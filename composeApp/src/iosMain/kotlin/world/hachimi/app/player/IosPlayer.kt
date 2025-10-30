package world.hachimi.app.player

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.*
import platform.CoreMedia.CMTimeCompare
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.*
import platform.MediaPlayer.*
import platform.UIKit.UIImage
import world.hachimi.app.logging.Logger

@OptIn(ExperimentalForeignApi::class)
class IosPlayer : Player {
    private var session: AVAudioSession? = null
    private var player: AVPlayer? = null
    private var isPlayerReady = false
    private val listeners = mutableSetOf<Player.Listener>()
    private var timeObserverToken: Any? = null
    val nowPlayingCenter = MPNowPlayingInfoCenter.defaultCenter()

    override suspend fun isPlaying(): Boolean {
        return player?.timeControlStatus == AVPlayerTimeControlStatusPlaying
    }

    override suspend fun isEnd(): Boolean {
        return player?.let {
            val durationSeconds = player?.currentItem?.duration
            val current = player?.currentItem?.currentTime()
            if (durationSeconds != null && current != null) {
                CMTimeCompare(current, durationSeconds) >= 0
            } else {
                false
            }
        } ?: false
    }

    override suspend fun currentPosition(): Long {
        return player?.let {
            val millis = it.currentTime().useContents {
                value.toDouble() * 1000 / timescale.toDouble()
            }
            millis.toLong()
        } ?: -1
    }

    override suspend fun play() {
        player?.play()
    }

    override suspend fun pause() {
        player?.pause()
    }

    override suspend fun seek(position: Long, autoStart: Boolean) {
        val time = CMTimeMakeWithSeconds(position.toDouble() / 1000, 1000)
        player?.seekToTime(time) { completed ->
            if (completed && autoStart) {
                player?.play()
            }
        }
    }

    override suspend fun getVolume(): Float {
        return player?.volume ?: 1f
    }

    override suspend fun setVolume(value: Float) {
        player?.volume = value
    }

    override suspend fun prepare(item: SongItem, autoPlay: Boolean) {
        // Write bytes to a temporary file
        val url = withContext(Dispatchers.IO) {
            val tempDir = NSFileManager.defaultManager.temporaryDirectory
            val tempFile =
                tempDir.URLByAppendingPathComponent("temp_audio.${item.format}") ?: error("Could not create temp file")

            item.audioBytes.usePinned { pinned ->
                NSFileManager.defaultManager.createFileAtPath(
                    tempFile.path!!,
                    NSData.dataWithBytes(pinned.addressOf(0), item.audioBytes.size.toULong()),
                    null
                )
            }
            val url = NSURL.fileURLWithPath(tempFile.path!!)
            url
        }
        Logger.i("player", "temp url: ${url.absoluteString}")

        val playerItem = AVPlayerItem.playerItemWithURL(url)
        player?.replaceCurrentItemWithPlayerItem(playerItem)

        NSNotificationCenter.defaultCenter.addObserverForName(
            AVPlayerItemDidPlayToEndTimeNotification,
            null,
            null
        ) { _ ->
            Logger.i("player", "End")
            listeners.forEach { it.onEvent(PlayEvent.End) }
        }

        NSNotificationCenter.defaultCenter.addObserverForName(
            AVPlayerRateDidChangeNotification,
            null,
            null
        ) { _ ->
            when (player?.timeControlStatus) {
                AVPlayerTimeControlStatusPlaying -> {
                    Logger.i("player", "Play")
                    listeners.forEach { it.onEvent(PlayEvent.Play) }
                }
                AVPlayerTimeControlStatusPaused -> {
                    Logger.i("player", "Pause")
                    listeners.forEach { it.onEvent(PlayEvent.Pause) }
                }
                else -> {}
            }
        }

        val image = item.coverBytes?.usePinned {
            UIImage(NSData.dataWithBytes(it.addressOf(0), item.coverBytes.size.toULong()))
        }?.let {
            MPMediaItemArtwork(it)
        }

        val info = mapOf(
            MPMediaItemPropertyTitle to item.title,
            MPMediaItemPropertyArtist to item.artist,
            MPMediaItemPropertyPlaybackDuration to item.durationSeconds,
            MPMediaItemPropertyArtwork to image
        )

        @Suppress("UNCHECKED_CAST")
        nowPlayingCenter.nowPlayingInfo = info as Map<Any?, *>?

        if (autoPlay) player?.play()
    }

    override suspend fun isReady(): Boolean {
        return isPlayerReady
    }

    override suspend fun release() {
        timeObserverToken?.let { player?.removeTimeObserver(it) }
        NSNotificationCenter.defaultCenter.removeObserver(this)
        player?.pause()
//        player?.removeObserver(timeControlObserver, "timeControlStatus")
        player = null
        isPlayerReady = false
    }

    override suspend fun initialize() {
        val session = AVAudioSession.sharedInstance()
        this.session = session
        session.setCategory(category = AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, null)
        player = AVPlayer()

        isPlayerReady = true
    }

    override fun addListener(listener: Player.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
    }

    /*private val timeControlObserver: NSObject = object : NSObject(), NSKeyValueObservingProtocol {

        override fun observeValueForKeyPath(
            keyPath: String?,
            ofObject: Any?,
            change: Map<Any?, *>?,
            context: COpaquePointer?
        ) {
            println("${keyPath} has been updated to: ${change!![NSKeyValueChangeNewKey]!!}")
        }
    }*/
}