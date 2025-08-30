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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlaylistViewModel
import java.util.Locale
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FooterPlayer() {
    val global = koinInject<GlobalStore>()
    val playerState = global.playerState

    AnimatedVisibility(
        visible = playerState.hasSong,
        modifier = Modifier.fillMaxWidth(),
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Row(Modifier.height(120.dp).padding(horizontal = 24.dp, vertical = 12.dp)) {
            Card(modifier = Modifier.aspectRatio(1f), onClick = {
                global.expandPlayer()
            }, colors = CardDefaults.outlinedCardColors(), elevation = CardDefaults.outlinedCardElevation()) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = playerState.songCoverUrl,
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.padding(start = 16.dp).width(200.dp)) {
                Text(playerState.songTitle, style = MaterialTheme.typography.titleMedium)
                Text(playerState.songAuthor, style = MaterialTheme.typography.titleMedium)
            }

            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                SongControl(
                    modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
                    isPlaying = playerState.isPlaying,
                    isLoading = playerState.isLoading,
                    loadingProgress = playerState.downloadProgress,
                    onPlayPauseClick = { global.playOrPause() },
                    onPreviousClick = { global.queuePrevious() },
                    onNextClick = { global.queueNext() }
                )

                Spacer(Modifier.height(12.dp))

                SongProgress(
                    durationMillis = playerState.songDurationSecs * 1000L,
                    currentMillis = playerState.currentMillis,
                    onProgressChange = {
                        global.setSongProgress(it)
                    }
                )
            }

            var queueExpanded by remember { mutableStateOf(false) }

            // TODO[refactor](footer): I really should not write this garbage. Refactor later.
            var tobeAddedSong by remember { mutableStateOf<Pair<Long, Long>?>(null) }

            IconButton(onClick = {
                tobeAddedSong = playerState.songId?.let { it to Random.nextLong() }
            }) {
                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, "Add To Playlist")
            }

            IconButton(onClick = { queueExpanded = true }) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, "Queue")
            }

            if (queueExpanded) Popup(
                alignment = Alignment.CenterEnd,
                onDismissRequest = { queueExpanded = false }
            ) {
                MusicQueue(onClose = {
                    queueExpanded = false
                })
            }

            AddToPlaylistDialog(tobeAddedSong?.first, tobeAddedSong?.second)
            CreatePlaylistDialog()
        }
    }
}

@Composable
fun SongControl(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isLoading: Boolean,
    loadingProgress: Float,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.Default.SkipPrevious, "Skip Previous")
        }
        IconButton(onClick = onPlayPauseClick, colors = IconButtonDefaults.filledIconButtonColors()) {
            if (isLoading) {
                if (loadingProgress == 0f) CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = LocalContentColor.current,
                    strokeWidth = 2.dp
                ) else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = LocalContentColor.current,
                        strokeWidth = 2.dp,
                        progress = { loadingProgress }
                    )
                }
            } else {
                if (isPlaying) {
                    Icon(Icons.Default.Pause, "Pause")
                } else {
                    Icon(Icons.Default.PlayArrow, "Play")
                }
            }
        }
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.SkipNext, "Skip Next")
        }
    }
}

@Composable
fun SongProgress(
    durationMillis: Long,
    currentMillis: Long,
    onProgressChange: (Float) -> Unit,
) {
    var isDragging by remember { mutableStateOf(false) }
    val playingProgress by derivedStateOf {
        (currentMillis.toDouble() / durationMillis).toFloat()
    }
    var draggingProgress by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }


    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatSongDuration(currentMillis.milliseconds),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
        )
        Box(
            Modifier.width(500.dp).height(6.dp).background(MaterialTheme.colorScheme.primaryContainer)
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
                        onProgressChange(draggingProgress)
                    }
                }
        ) {
            val progress = if (isDragging) draggingProgress else playingProgress
            Box(Modifier.fillMaxWidth(progress).height(6.dp).background(MaterialTheme.colorScheme.primary))
        }
        Text(
            text = formatSongDuration(durationMillis.milliseconds),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Stable
fun formatSongDuration(duration: Duration): String {
    val seconds = duration.inWholeSeconds
    val minutesPart = seconds / 60
    val secondsPart = seconds % 60
    return "${minutesPart}:${String.format(Locale.US, "%02d", secondsPart)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToPlaylistDialog(
    tobeAddedSongId: Long?,
    random: Long?,
    vm: PlaylistViewModel = koinViewModel(),
) {
    LaunchedEffect(vm, tobeAddedSongId, random) {
        if (tobeAddedSongId != null) {
            vm.toBeAddedSongId = tobeAddedSongId
            vm.addToPlaylist()
        }
    }

    if (vm.showPlaylistDialog) BasicAlertDialog(
        modifier = Modifier.width(400.dp),
        onDismissRequest = {
            vm.cancelAddToPlaylist()
        },
        content = {
            ElevatedCard(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)) {
                Column(Modifier.padding(24.dp)) {
                    Text("收藏到歌单", style = MaterialTheme.typography.titleLarge)

                    Spacer(Modifier.height(16.dp))

                    Text("请选择一个歌单")

                    if (vm.playlistIsLoading) {
                        LinearProgressIndicator()
                    }

                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { vm.createPlaylist() }
                    ) {
                        Row(Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(
                                modifier = Modifier.padding(start = 16.dp),
                                text = "新建歌单"
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    LazyColumn(Modifier.height(200.dp).fillMaxWidth()) {
                        itemsIndexed(vm.playlists, key = { _, item -> item.id }) { index, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                onClick = {
                                    vm.selectedPlaylistId = item.id
                                }
                            ) {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(Modifier.size(64.dp), MaterialTheme.shapes.medium, LocalContentColor.current.copy(0.12f)) {
                                        AsyncImage(
                                            modifier = Modifier.fillMaxSize(),
                                            model = item.coverUrl,
                                            contentDescription = "Playlist Cover",
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = item.name
                                    )
                                    if (vm.selectedPlaylistId == item.id) {
                                        Icon(
                                            modifier = Modifier.padding(end = 12.dp),
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(Modifier.align(Alignment.End)) {
                        TextButton(onClick = { vm.cancelAddToPlaylist() }) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = { vm.confirmAddToPlaylist() },
                            enabled = vm.selectedPlaylistId != null && !vm.addingToPlaylistOperating
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun CreatePlaylistDialog(vm: PlaylistViewModel = koinViewModel()) {
    if (vm.showCreatePlaylistDialog) AlertDialog(
        title = { Text("新建歌单") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = vm.createPlaylistName,
                    onValueChange = { vm.createPlaylistName = it },
                    label = { Text("名称") },
                    singleLine = true
                )
                TextField(
                    value = vm.createPlaylistDescription,
                    onValueChange = { vm.createPlaylistDescription = it },
                    label = { Text("描述") },
                    minLines = 3,
                    maxLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("私有歌单")
                    Switch(
                        modifier = Modifier.padding(start = 16.dp),
                        checked = vm.createPlaylistPrivate,
                        onCheckedChange = { vm.createPlaylistPrivate = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmCreatePlaylist() },
                enabled = vm.createPlaylistName.isNotBlank() && !vm.createPlaylistOperating
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.cancelCreatePlaylist() }) {
                Text("取消")
            }
        },
        onDismissRequest = {
            vm.cancelCreatePlaylist()
        }
    )
}