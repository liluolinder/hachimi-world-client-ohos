package world.hachimi.app.ui.root.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.util.formatDuration
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun FooterPlayer() {
    val global = koinInject<GlobalStore>()
    val playerState = global.playerState

    AnimatedVisibility(
        visible = playerState.hasSong,
        modifier = Modifier.height(120.dp).fillMaxWidth(),
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Row(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            Card(Modifier.aspectRatio(1f)) {
                playerState.songCoverUrl?.let {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = playerState.songCoverUrl,
                        contentDescription = "Cover",
                    )
                }
            }

            Column(Modifier.padding(start = 16.dp)) {
                Text(playerState.songTitle, style = MaterialTheme.typography.titleMedium)
                Text(playerState.songAuthor, style = MaterialTheme.typography.titleMedium)
            }

            Column(Modifier.weight(1f), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 12.dp).align(androidx.compose.ui.Alignment.CenterHorizontally)) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.SkipPrevious, "Skip Previous")
                    }
                    IconButton(onClick = {
                        global.playOrPause()
                    }, colors = IconButtonDefaults.filledIconButtonColors()) {
                        if (playerState.isLoading) {
                            if (playerState.downloadProgress == 0f) CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = LocalContentColor.current,
                                strokeWidth = 2.dp
                            ) else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = LocalContentColor.current,
                                    strokeWidth = 2.dp,
                                    progress = {
                                        playerState.downloadProgress
                                    }
                                )
                            }
                        } else {
                            if (playerState.isPlaying) {
                                Icon(Icons.Default.Pause, "Pause")
                            } else {
                                Icon(Icons.Default.PlayArrow, "Play")
                            }
                        }
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.SkipNext, "Skip Next")
                    }
                }
                Spacer(Modifier.height(12.dp))

                var isDragging by remember { mutableStateOf(false) }
                val playingProgress by derivedStateOf {
                    (playerState.currentSongPositionMs.toDouble() / (playerState.songDurationSecs * 1000.0)).toFloat()
                }
                var draggingProgress by remember { mutableStateOf(0f) }
                var offsetX by remember { mutableStateOf(0f) }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatSongDuration(playerState.currentSongPositionMs.milliseconds),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                    )
                    Box(Modifier.width(500.dp).height(6.dp).background(MaterialTheme.colorScheme.primaryContainer)
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                offsetX = down.position.x
                                draggingProgress = down.position.x / size.width
                                isDragging = true

                                while (true) {
                                    val change = awaitDragOrCancellation(down.id)
                                    if (change != null && change.pressed) {
                                        val summed = offsetX + change.positionChange().x
                                        change.consume()
                                        offsetX = summed
                                        draggingProgress = summed / size.width
                                    } else {
                                        break
                                    }
                                }
                                isDragging = false
                                global.setSongProgress(draggingProgress)
                            }
                        }
                    ) {
                        val progress = if (isDragging) draggingProgress else playingProgress
                        Box(Modifier.fillMaxWidth(progress).height(6.dp).background(MaterialTheme.colorScheme.primary))
                    }
                    Text(
                        text = formatSongDuration(playerState.songDurationSecs.seconds),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }

            }
        }
    }

}

@Stable
fun formatSongDuration(duration: Duration): String {
    val seconds = duration.inWholeSeconds
    val minutesPart = seconds / 60
    val secondsPart = seconds % 60
    return "${minutesPart}:${String.format(Locale.US,"%02d", secondsPart)}"
}