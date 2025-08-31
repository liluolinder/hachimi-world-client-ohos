package world.hachimi.app.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil3.compose.AsyncImage
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlayerUIState
import world.hachimi.app.ui.player.components.Lyrics
import world.hachimi.app.ui.player.components.SongControl
import world.hachimi.app.ui.player.components.SongProgress
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun PlayerScreen() {
    val global: GlobalStore = koinInject()
    BoxWithConstraints {
        if (maxWidth < 600.dp) {
            CompactPlayerScreen(
                playerState = global.playerState,
                onShrinkClick = { global.shrinkPlayer() },
                onPlayOrPauseClick = { global.playOrPause() },
                onPreviousClick = { global.queuePrevious() },
                onNextClick = { global.queueNext() },
                onProgressChange = { global.setSongProgress(it) }
            )
        } else {
            ExpandedPlayerScreen(
                playerState = global.playerState,
                onShrinkClick = { global.shrinkPlayer() },
                onPlayOrPauseClick = { global.playOrPause() },
                onPreviousClick = { global.queuePrevious() },
                onNextClick = { global.queueNext() },
                onProgressChange = { global.setSongProgress(it) }
            )
        }
    }
}

@Composable
fun CompactPlayerScreen(
    playerState: PlayerUIState,
    onShrinkClick: () -> Unit,
    onPlayOrPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onProgressChange: (Float) -> Unit,
) {
    Surface(Modifier.fillMaxSize()) {
        var displayingLyrics by remember { mutableStateOf(false) }

        Column(Modifier.systemBarsPadding()) {
            if (displayingLyrics) {
                Lyrics(
                    modifier = Modifier.fillMaxWidth().weight(1f).clickable(
                        indication = null,
                        interactionSource = null,
                        onClick = { displayingLyrics = false }
                    ).padding(horizontal = 24.dp),
                    currentLine = playerState.currentLyricsLine,
                    lines = playerState.lyricsLines
                )
            } else Column(Modifier.weight(1f).padding(horizontal = 48.dp, vertical = 24.dp)) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(vertical = 24.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    onClick = { displayingLyrics = true }
                ) {
                    AsyncImage(
                        model = playerState.songCoverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = playerState.songTitle,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = playerState.songAuthor,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(0.6f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "基米ID：${playerState.songDisplayId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalContentColor.current.copy(0.7f)
                )
            }

            Column(
                Modifier.fillMaxWidth().padding(
                    top = 12.dp,
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 24.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Current lyric line
                if (!displayingLyrics) {
                    playerState.lyricsLines.getOrNull(playerState.currentLyricsLine)?.let { lyricsLine ->
                        Text(text = lyricsLine)
                    }
                }

                SongControl(
                    modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
                    isPlaying = playerState.isPlaying,
                    isLoading = playerState.isLoading,
                    loadingProgress = playerState.downloadProgress,
                    onPlayPauseClick = onPlayOrPauseClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick
                )

                Spacer(Modifier.height(12.dp))

                SongProgress(
                    durationMillis = playerState.songDurationSecs * 1000L,
                    currentMillis = playerState.currentMillis,
                    onProgressChange = onProgressChange
                )
            }
        }
    }
}

@Composable
fun ExpandedPlayerScreen(
    playerState: PlayerUIState,
    onShrinkClick: () -> Unit,
    onPlayOrPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onProgressChange: (Float) -> Unit,
) {
    Surface {
        Box {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().weight(1f).padding(32.dp)) {
                    Column(Modifier.fillMaxHeight().weight(1f), verticalArrangement = Arrangement.Center) {
                        Column(Modifier.align(Alignment.End).padding(48.dp)) {
                            BoxWithConstraints(Modifier.wrapContentSize()) {
                                val size = min(maxHeight * 0.7f, maxWidth)
                                ElevatedCard(
                                    modifier = Modifier.size(size),
                                    elevation = CardDefaults.cardElevation(12.dp)
                                ) {
                                    AsyncImage(
                                        model = playerState.songCoverUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = playerState.songTitle,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = playerState.songAuthor,
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalContentColor.current.copy(0.6f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "基米ID：${playerState.songDisplayId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalContentColor.current.copy(0.7f)
                            )
                        }
                    }

                    Spacer(Modifier.width(64.dp))

                    Lyrics(
                        currentLine = playerState.currentLyricsLine,
                        lines = playerState.lyricsLines,
                        modifier = Modifier.fillMaxHeight().weight(1f)
                    )
                }

                Column(
                    Modifier.fillMaxWidth().padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 24.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SongControl(
                        modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
                        isPlaying = playerState.isPlaying,
                        isLoading = playerState.isLoading,
                        loadingProgress = playerState.downloadProgress,
                        onPlayPauseClick = onPlayOrPauseClick,
                        onPreviousClick = onPreviousClick,
                        onNextClick = onNextClick
                    )

                    Spacer(Modifier.height(12.dp))

                    SongProgress(
                        durationMillis = playerState.songDurationSecs * 1000L,
                        currentMillis = playerState.currentMillis,
                        onProgressChange = onProgressChange
                    )
                }
            }

            IconButton(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                onClick = onShrinkClick
            ) {
                Icon(Icons.Default.CloseFullscreen, "Shrink")
            }
        }
    }
}

@Composable
private fun rememberTestPlayerState(): PlayerUIState {
    val state = remember {
        PlayerUIState().apply {
            songTitle = "Test song title"
            songAuthor = "Test song author"
            songCoverUrl = null
            songDisplayId = "JM-AWSL-123"
            hasSong = true
            songDurationSecs = 365
            setLyrics("[00:00.00] Test lyrics line\n[01:00.00] Test lyrics line 2")
            updateCurrentMillis(100L)
        }
    }
    return state
}

@Preview
@Composable
private fun PreviewExpanded() {
    val playerUIState = rememberTestPlayerState()
    Box(Modifier.requiredWidth(1200.dp)) {
        PreviewTheme(background = true) {
            ExpandedPlayerScreen(
                playerState = playerUIState,
                onShrinkClick = {},
                onPlayOrPauseClick = {},
                onPreviousClick = {},
                onNextClick = {},
                onProgressChange = {}
            )
        }
    }
}

@Preview
@Composable
private fun PreviewCompact() {
    val playerUIState = rememberTestPlayerState()
    Box(Modifier.requiredWidth(1200.dp)) {
        PreviewTheme(background = true) {
            CompactPlayerScreen(
                playerState = playerUIState,
                onShrinkClick = {},
                onPlayOrPauseClick = {},
                onPreviousClick = {},
                onNextClick = {},
                onProgressChange = {}
            )
        }
    }
}
