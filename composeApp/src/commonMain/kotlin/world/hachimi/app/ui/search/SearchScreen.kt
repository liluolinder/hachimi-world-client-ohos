package world.hachimi.app.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.SearchViewModel
import world.hachimi.app.util.formatSongDuration
import kotlin.time.Duration.Companion.seconds

@Composable
fun SearchScreen(
    query: String,
    vm: SearchViewModel = koinViewModel(),
) {
    val global = koinInject<GlobalStore>()
    DisposableEffect(vm, query) {
        vm.mounted(query)
        onDispose {
            vm.dispose()
        }
    }

    if (vm.loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    } else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp)) {
        item {
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "搜索结果",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${vm.searchProcessingTimeMs} ms",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        item {
            SingleChoiceSegmentedButtonRow(Modifier.padding(bottom = 12.dp)) {
                SegmentedButton(
                    selected = true,
                    onClick = {},
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 1),
                    label = { Text("歌曲") }
                )
            }
        }
        item {
            if (vm.data.isEmpty()) Box(Modifier.fillMaxWidth().padding(vertical = 128.dp), contentAlignment = Alignment.Center) {
                Text("空空如也")
            }
        }
        itemsIndexed(
            items = vm.data,
            key = { _, item -> item.displayId }
        ) { index, item ->
            SearchSongItem(item, Modifier.widthIn(min = 300.dp).padding(vertical = 12.dp), {
                global.player.insertToQueue(item.displayId, true, false)
            })
        }
    }
}

@Composable
private fun SearchSongItem(
    data: SongModule.SearchSongItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier.height(120.dp),
        onClick = onClick
    ) {
        Row {
            AsyncImage(
                model = data.coverArtUrl,
                contentDescription = null,
                modifier = Modifier.aspectRatio(1f).fillMaxWidth().padding(12.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.weight(1f).padding(start = 12.dp, top = 12.dp)) {
                Text(data.title, style = MaterialTheme.typography.titleMedium)
                Text(data.artist, style = MaterialTheme.typography.titleSmall)
            }
            Text(
                modifier = Modifier.padding(12.dp),
                text = formatSongDuration(data.durationSeconds.seconds), style = MaterialTheme.typography.bodySmall
            )
        }
    }
    /*SongCard(
        coverUrl = data.coverArtUrl,
        title = data.title,
        subtitle = data.subtitle,
        author = data.artist,
        tags = emptyList(),// data.tags.map { it.name },
        likeCount = data.likeCount,
        onClick = onClick,
        modifier = modifier,
    )*/
}