package world.hachimi.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.MainViewModel

@Composable
fun HomeScreen(vm: MainViewModel = koinViewModel()) {
    val global = koinInject<GlobalStore>()
    DisposableEffect(vm) {
        vm.mounted()
        onDispose {
            vm.unmount()
        }
    }

    Column(Modifier.fillMaxSize()) {
        Text("推荐音乐", style = MaterialTheme.typography.headlineSmall)

        if (vm.isLoading) {
            CircularProgressIndicator()
        }

        LazyVerticalGrid(GridCells.Adaptive(minSize = 180.dp), modifier = Modifier.fillMaxSize(),) {
            itemsIndexed(vm.songs, key = { index, item -> item.id }) { index, item ->
                SongCard(
                    item.coverUrl,
                    item.title,
                    item.subtitle,
                    item.uploaderUid.toString(),
                    item.tags.map { it.name },
                    item.likeCount,
                    onClick = {
                        global.playSong(item.id)
                    },
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                )
            }
        }

        /*Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            vm.songs.forEach {
                SongCard(
                    it.coverUrl,
                    it.title,
                    it.subtitle,
                    it.uploaderUid.toString(),
                    it.tags.map { it.name },
                    it.likeCount,
                    onClick = {
                        global.expandPlayer()
                    },
                    modifier = Modifier.width(240.dp),
                )
            }
        }*/
    }
}


@Composable
private fun SongCard(
    coverUrl: String,
    title: String,
    subtitle: String,
    author: String,
    tags: List<String>,
    likeCount: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier, onClick = onClick) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                AsyncImage(coverUrl, null, Modifier.fillMaxSize())
                Row(Modifier.align(Alignment.BottomStart).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                text= tag, style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                /*Row(Modifier.align(Alignment.BottomEnd).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Favorite, "Like", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                    Text(likeCount.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                }*/
            }

            Column(Modifier.padding(vertical = 8.dp, horizontal = 8.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Text(author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)


            }
        }
    }
}