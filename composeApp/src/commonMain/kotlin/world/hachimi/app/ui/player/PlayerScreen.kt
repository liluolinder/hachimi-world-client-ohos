package world.hachimi.app.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.fastForEach
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlayerUIState
import world.hachimi.app.model.SongDetailInfo
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.player.components.Album
import world.hachimi.app.ui.player.components.Lyrics
import world.hachimi.app.ui.player.components.SongControl
import world.hachimi.app.ui.player.components.SongProgress
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.WindowSize

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerScreen() {
    val global: GlobalStore = koinInject()
    BackHandler {
        global.shrinkPlayer()
    }
    BoxWithConstraints {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {
            if (maxWidth < WindowSize.MEDIUM) {
                CompactPlayerScreen(
                    playerState = global.player.playerState,
                    onShrinkClick = { global.shrinkPlayer() },
                    onPlayOrPauseClick = { global.player.playOrPause() },
                    onPreviousClick = { global.player.queuePrevious() },
                    onNextClick = { global.player.queueNext() },
                    onProgressChange = { global.player.setSongProgress(it) },
                    onNavToAuthor = { uid ->
                        global.shrinkPlayer()
                        global.nav.push(Route.Root.PublicUserSpace(uid))
                    }
                )
            } else {
                ExpandedPlayerScreen(
                    playerState = global.player.playerState,
                    onShrinkClick = { global.shrinkPlayer() },
                    onPlayOrPauseClick = { global.player.playOrPause() },
                    onPreviousClick = { global.player.queuePrevious() },
                    onNextClick = { global.player.queueNext() },
                    onProgressChange = { global.player.setSongProgress(it) },
                    onNavToAuthor = { uid ->
                        global.shrinkPlayer()
                        global.nav.push(Route.Root.PublicUserSpace(uid))
                    }
                )
            }
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
    onNavToAuthor: (Long) -> Unit
) {
    var displayingLyrics by remember { mutableStateOf(false) }
    // TODO: Remove these, use new UI/UX to indicate loading status
    val previewMetadata = playerState.previewMetadata
    val info = playerState.songInfo

    val displayedId = if (playerState.fetchingMetadata) { previewMetadata?.displayId } else { info?.displayId } ?: ""
    val displayedCover = if (playerState.fetchingMetadata) previewMetadata?.coverUrl else info?.coverUrl
    val displayedTitle = if (playerState.fetchingMetadata) { previewMetadata?.title } else { info?.title } ?: ""
    val displayedAuthor = if (playerState.fetchingMetadata) { previewMetadata?.author } else { info?.uploaderName } ?: ""

    Column(Modifier.systemBarsPadding()) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            val lyricsAlpha by animateFloatAsState(if (displayingLyrics) 1f else 0f)
            Column(
                Modifier
                    .graphicsLayer { alpha = 1f - lyricsAlpha }
                    .padding(horizontal = 48.dp, vertical = 24.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(
                        modifier = Modifier.align(Alignment.Start),
                        text = displayedId,
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalContentColor.current.copy(0.7f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Album(
                        modifier = Modifier.fillMaxWidth(),
                        coverUrl = displayedCover,
                        onClick = { displayingLyrics = true },
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = displayedTitle,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (!playerState.fetchingMetadata) {
                        playerState.songInfo?.subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleSmall,
                                color = LocalContentColor.current.copy(0.7f)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            modifier = Modifier.clickable(indication = null, interactionSource = null, onClick = {
                                playerState.songInfo?.uploaderUid?.let {
                                    onNavToAuthor(it)
                                }
                            }),
                            text = "作者: ${displayedAuthor}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalContentColor.current.copy(0.7f)
                        )

                        /*playerState.staff.fastForEach { (role, name) ->
                            Text(
                                text = "${role}: ${name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalContentColor.current.copy(0.7f)
                            )
                        }*/
                    }
                }
/*
                Text(
                    text = playerState.songTitle,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    modifier = Modifier.clickable(indication = null, interactionSource = null, onClick = {
                        playerState.songInfo?.uploaderUid?.let {
                            onNavToAuthor(it)
                        }
                    }),
                    text = playerState.songAuthor,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(0.6f)
                )*/

                // Current lyric line
                val lyricsLine =
                    remember { derivedStateOf { playerState.lyricsLines.getOrNull(playerState.currentLyricsLine) } }

                AnimatedContent(lyricsLine.value, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(
                        modifier = Modifier.fillMaxWidth().wrapContentWidth(),
                        text = it ?: ""
                    )
                }
            }

            Lyrics(
                modifier = Modifier.graphicsLayer {
                    // Hide and do not consume pointer input
                    this.scaleX = if (lyricsAlpha == 0f) 0f else 1f
                    this.scaleY = if (lyricsAlpha == 0f) 0f else 1f
                    this.alpha = lyricsAlpha
                }.fillMaxWidth().clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = { displayingLyrics = false },
                    enabled = displayingLyrics
                ).padding(horizontal = 24.dp),
                currentLine = playerState.currentLyricsLine,
                lines = playerState.lyricsLines,
                fadeColor = MaterialTheme.colorScheme.surfaceVariant
            )

            IconButton(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                onClick = onShrinkClick
            ) {
                Icon(Icons.Default.CloseFullscreen, "Shrink")
            }
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
            SongControl(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                isPlaying = playerState.isPlaying,
                isLoading = playerState.buffering,
                loadingProgress = { playerState.downloadProgress },
                onPlayPauseClick = onPlayOrPauseClick,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick
            )

            Spacer(Modifier.height(12.dp))

            SongProgress(
                durationMillis = if (playerState.fetchingMetadata) {
                    previewMetadata?.duration?.inWholeMilliseconds
                } else {
                    info?.durationSeconds?.let {
                        it * 1000L
                    }
                } ?: -1L,
                currentMillis = if (playerState.fetchingMetadata) {
                    0L
                } else {
                    playerState.currentMillis
                },
                onProgressChange = onProgressChange
            )
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
    onNavToAuthor: (Long) -> Unit
) {
    val previewMetadata = playerState.previewMetadata
    val info = playerState.songInfo

    val displayedId = if (playerState.fetchingMetadata) { previewMetadata?.displayId } else { info?.displayId } ?: ""
    val displayedCover = if (playerState.fetchingMetadata) previewMetadata?.coverUrl else info?.coverUrl
    val displayedTitle = if (playerState.fetchingMetadata) { previewMetadata?.title } else { info?.title } ?: ""
    val displayedAuthor = if (playerState.fetchingMetadata) { previewMetadata?.author } else { info?.uploaderName } ?: ""

    Box {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().weight(1f).padding(32.dp)) {
                Column(Modifier.fillMaxHeight().weight(1f), verticalArrangement = Arrangement.Center) {
                    Column(Modifier.align(Alignment.End).padding(48.dp)) {
                        Text(
                            modifier = Modifier.align(Alignment.Start),
                            text = displayedId,
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalContentColor.current.copy(0.7f)
                        )
                        Spacer(Modifier.height(8.dp))
                        BoxWithConstraints(Modifier.wrapContentSize()) {
                            val size = min(maxHeight * 0.7f, maxWidth)

                            Album(
                                coverUrl = displayedCover,
                                onClick = {},
                                modifier = Modifier.size(size)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = displayedTitle,
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (!playerState.fetchingMetadata) {
                            info?.subtitle?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = LocalContentColor.current.copy(0.7f)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                modifier = Modifier.clickable(indication = null, interactionSource = null, onClick = {
                                    info?.uploaderUid?.let {
                                        onNavToAuthor(it)
                                    }
                                }),
                                text = "作者: ${displayedAuthor}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalContentColor.current.copy(0.7f)
                            )

                            if (!playerState.fetchingMetadata) {
                                info?.originInfos?.fastForEach { item ->
                                    Text(
                                        text = "原作: ${item.title}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LocalContentColor.current.copy(0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.width(64.dp))

                Lyrics(
                    currentLine = playerState.currentLyricsLine,
                    lines = playerState.lyricsLines,
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    fadeColor = MaterialTheme.colorScheme.surfaceVariant
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
                    isLoading = playerState.buffering,
                    loadingProgress = { playerState.downloadProgress },
                    onPlayPauseClick = onPlayOrPauseClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick
                )

                Spacer(Modifier.height(12.dp))

                SongProgress(
                    durationMillis = if (playerState.fetchingMetadata) {
                        previewMetadata?.duration?.inWholeMilliseconds
                    } else {
                        info?.durationSeconds?.let {
                            it * 1000L
                        }
                    } ?: -1L,
                    currentMillis = if (playerState.fetchingMetadata) {
                        0L
                    } else {
                        playerState.currentMillis
                    },
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

@Composable
private fun rememberTestPlayerState(): PlayerUIState {
    val state = remember {
        PlayerUIState().apply {
            updateSongInfo(
                SongDetailInfo(
                    id = 0,
                    displayId = "JM-AWSL-123",
                    title = "Test Title",
                    subtitle = "Test Subtitle",
                    description = "",
                    durationSeconds = 365,
                    tags = emptyList(),
                    lyrics = "",
                    audioUrl = "",
                    coverUrl = "",
                    productionCrew = emptyList(),
                    creationType = 0,
                    originInfos = emptyList(),
                    uploaderUid = 100000,
                    uploaderName = "",
                    playCount = 0,
                    likeCount = 0,
                    externalLinks = listOf(
                        SongModule.ExternalLink("bilibili", "https://xxxxxxx")
                    )
                )
            )
            hasSong = true
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
                onProgressChange = {},
                onNavToAuthor = {}
            )
        }
    }
}

@Preview
@Composable
private fun PreviewCompact() {
    val playerUIState = rememberTestPlayerState()
    PreviewTheme(background = true) {
        CompactPlayerScreen(
            playerState = playerUIState,
            onShrinkClick = {},
            onPlayOrPauseClick = {},
            onPreviousClick = {},
            onNextClick = {},
            onProgressChange = {},
            onNavToAuthor = {}
        )
    }
}
