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
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.MainViewModel
import world.hachimi.app.ui.home.components.SongCard

@Composable
fun HomeScreen(vm: MainViewModel = koinViewModel()) {
    val global = koinInject<GlobalStore>()
    DisposableEffect(vm) {
        vm.mounted()
        onDispose {
            vm.unmount()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.padding(top = 24.dp),
                text ="推荐音乐", style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(GridCells.Adaptive(minSize = 180.dp), modifier = Modifier.fillMaxSize(),) {
                itemsIndexed(vm.songs, key = { index, item -> item.id }) { index, item ->
                    SongCard(
                        item.coverUrl,
                        item.title,
                        item.subtitle,
                        item.uploaderName,
                        item.tags.map { it.name },
                        item.likeCount,
                        onClick = {
                            global.insertToQueue(item.displayId, true, false)
                        },
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                    )
                }
            }
        }

        if (vm.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.TopCenter).padding(top = 24.dp))
        }
    }
}

