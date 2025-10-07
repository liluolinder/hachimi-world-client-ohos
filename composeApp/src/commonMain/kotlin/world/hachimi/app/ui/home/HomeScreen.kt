package world.hachimi.app.ui.home

import androidx.compose.animation.AnimatedContent
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
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.MainViewModel
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.home.components.SongCard

@Composable
fun HomeScreen(vm: MainViewModel = koinViewModel()) {
    val global = koinInject<GlobalStore>()
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.unmount() }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.padding(top = 24.dp, start = 24.dp),
                text = "推荐音乐", style = MaterialTheme.typography.titleLarge
            )

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
                            columns = GridCells.Adaptive(minSize = 160.dp),
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
                                        global.player.insertToQueue(item.displayId, true, false)
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

