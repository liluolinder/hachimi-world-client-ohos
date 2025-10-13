package world.hachimi.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.MainViewModel
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.home.components.SongCard
import world.hachimi.app.util.WindowSize
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: MainViewModel = koinViewModel()) {
    val global = koinInject<GlobalStore>()
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.unmount() }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxWidth = maxWidth
        AdaptivePullToRefreshBox(
            isRefreshing = vm.isLoading,
            onRefresh = {
                vm.fakeRefresh()
            },
            screenWidth = maxWidth
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "最近发布", style = MaterialTheme.typography.titleLarge
                    )
                    if (maxWidth >= WindowSize.COMPACT) {
                        IconButton(
                            modifier = Modifier.padding(start = 8.dp),
                            enabled = !vm.isLoading,
                            onClick = { vm.fakeRefresh() }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        modifier = Modifier,
                        onClick = { vm.playAllRecent() }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                        Spacer(Modifier.width(8.dp))
                        Text("播放全部")
                    }
                }

                Spacer(Modifier.height(24.dp))

                AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
                    when (it) {
                        InitializeStatus.INIT -> LoadingPage()
                        InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
                        InitializeStatus.LOADED -> {
                            if (vm.songs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("空空如也")
                            } else LazyVerticalGrid(
                                modifier = Modifier.fillMaxSize(),
                                columns = if (maxWidth < 400.dp) GridCells.Adaptive(minSize = 120.dp) else GridCells.Adaptive(
                                    minSize = 160.dp
                                ),
                                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                itemsIndexed(vm.songs, key = { index, item -> item.id }) { index, item ->
                                    SongCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        coverUrl = item.coverUrl,
                                        title = item.title,
                                        subtitle = item.subtitle,
                                        author = item.uploaderName,
                                        tags = item.tags.map { it.name },
                                        likeCount = item.likeCount,
                                        playCount = item.playCount,
                                        onClick = {
                                            global.player.insertToQueue(
                                                GlobalStore.MusicQueueItem(
                                                    id = item.id,
                                                    displayId = item.displayId,
                                                    name = item.title,
                                                    artist = item.uploaderName,
                                                    duration = item.durationSeconds.seconds,
                                                    coverUrl = item.coverUrl
                                                ), true, false
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptivePullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    screenWidth: Dp,
    content: @Composable BoxScope.() -> Unit
) {
    if (screenWidth >= WindowSize.COMPACT) {
        Box(content = content)
    } else PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        content = content
    )
}
