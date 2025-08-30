package world.hachimi.app.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.player.components.Lyrics
import world.hachimi.app.ui.player.components.SongControl
import world.hachimi.app.ui.player.components.SongProgress

@Composable
fun PlayerScreen() {
    val global = koinInject<GlobalStore>()

    Surface {
        Box {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().weight(1f).padding(32.dp)) {
                    Column(Modifier.fillMaxHeight().weight(1f), verticalArrangement = Arrangement.Center) {
                        Column(Modifier.align(Alignment.End).padding(48.dp)) {
                            BoxWithConstraints(Modifier.wrapContentSize()) {
                                val size = min(maxHeight * 0.7f, maxWidth)
                                Card(
                                    modifier = Modifier.size(size),
                                    elevation = CardDefaults.cardElevation(12.dp)
                                ) {
                                    AsyncImage(
                                        model = global.playerState.songCoverUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = global.playerState.songTitle,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = global.playerState.songAuthor,
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalContentColor.current.copy(0.6f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "基米ID：${global.playerState.songDisplayId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalContentColor.current.copy(0.7f)
                            )
                        }
                    }

                    Spacer(Modifier.width(64.dp))

                    Lyrics(
                        currentLine = global.playerState.currentLyricsLine,
                        lines = global.playerState.lyricsLines,
                        modifier = Modifier.fillMaxHeight().weight(1f)
                    )
                }

                Column(Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    SongControl(
                        modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
                        isPlaying = global.playerState.isPlaying,
                        isLoading = global.playerState.isLoading,
                        loadingProgress = global.playerState.downloadProgress,
                        onPlayPauseClick = { global.playOrPause() },
                        onPreviousClick = { global.queuePrevious() },
                        onNextClick = { global.queueNext() }
                    )

                    Spacer(Modifier.height(12.dp))

                    SongProgress(
                        durationMillis = global.playerState.songDurationSecs * 1000L,
                        currentMillis = global.playerState.currentMillis,
                        onProgressChange = { global.setSongProgress(it) }
                    )
                }
            }

            IconButton(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                onClick = { global.shrinkPlayer() }
            ) {
                Icon(Icons.Default.CloseFullscreen, "Shrink")
            }
        }
    }
}
