package world.hachimi.app.ui.playlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.time.Instant
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage

@Composable
fun PlaylistScreen(vm: PlaylistViewModel = koinViewModel()) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }
    val global = koinInject<GlobalStore>()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(modifier = Modifier.fillMaxWidth(), text = "我的歌单", style = MaterialTheme.typography.titleLarge)
        AnimatedContent(vm.initializeStatus, modifier = Modifier.weight(1f)) {
            when (it) {
                InitializeStatus.INIT -> LoadingPage()
                InitializeStatus.FAILED -> ReloadPage(onReloadClick = { vm.retry() })
                InitializeStatus.LOADED -> Box(Modifier.fillMaxSize()) {
                    if (vm.playlists.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("空空如也")
                    } else LazyVerticalGrid(GridCells.Adaptive(minSize = 180.dp), modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(vm.playlists, key = { index, item -> item.id }) { index, item ->
                            PlaylistItem(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                coverUrl = item.coverUrl,
                                title = item.name,
                                songCount = item.songsCount,
                                createTime = item.createTime,
                                onEnter = {
                                    global.nav.push(Route.Root.MyPlaylist.Detail(item.id))
                                }
                            )
                        }
                    }
                    if (vm.playlistIsLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistItem(
    coverUrl: String?,
    title: String,
    songCount: Int,
    createTime: Instant,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier, onClick = onEnter) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = coverUrl,
                        contentDescription = "Playlist Cover Image",
                        contentScale = ContentScale.Crop
                    )
                }
                Row(
                    Modifier.align(Alignment.BottomStart).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            text = "$songCount 首", style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Column(Modifier.padding(vertical = 8.dp, horizontal = 8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                /*Text(
                    text = author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )*/
            }
        }
    }
}