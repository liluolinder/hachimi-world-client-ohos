package world.hachimi.app.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.*
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import org.koin.android.ext.android.get
import world.hachimi.app.MainActivity
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlayerService

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService(), MediaSession.Callback {
    private var mediaSession: MediaSession? = null
    private var globalPlayer: PlayerService = get<GlobalStore>().player

    companion object {
        private val PREVIOUS_ACTION = "custom_previous"
        private val NEXT_ACTION = "custom_next"


        private val previousButton = CommandButton.Builder(CommandButton.ICON_PREVIOUS)
            .setDisplayName("Previous")
            .setSessionCommand(SessionCommand(PREVIOUS_ACTION, Bundle.EMPTY))
            .setSlots(CommandButton.SLOT_BACK)
            .build()

        private val nextButton = CommandButton.Builder(CommandButton.ICON_NEXT)
            .setDisplayName("Next")
            .setSessionCommand(SessionCommand(NEXT_ACTION, Bundle.EMPTY))
            .setSlots(CommandButton.SLOT_FORWARD)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        player.addAnalyticsListener(EventLogger())

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(this)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setCustomLayout(listOf(previousButton, nextButton))
            .setMediaButtonPreferences(listOf(
                previousButton, nextButton
            ))
            .build()
    }

    override fun onGetSession(p0: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }


    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
            .add(SessionCommand(PREVIOUS_ACTION, Bundle.EMPTY))
            .add(SessionCommand(NEXT_ACTION, Bundle.EMPTY))
            .build()
        return MediaSession.ConnectionResult.accept(
            availableSessionCommands,
            connectionResult.availablePlayerCommands
        )
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        when (customCommand.customAction) {
            PREVIOUS_ACTION -> globalPlayer.previous()
            NEXT_ACTION -> globalPlayer.next()
            else -> return super.onCustomCommand(session, controller, customCommand, args)
        }
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }
}