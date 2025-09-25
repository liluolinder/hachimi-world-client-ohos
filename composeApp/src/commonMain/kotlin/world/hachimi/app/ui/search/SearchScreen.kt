package world.hachimi.app.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.SearchViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.search.components.SearchSongItem
import world.hachimi.app.ui.search.components.SearchUserItem

@Composable
fun SearchScreen(
    query: String,
    searchType: SearchViewModel.SearchType,
    vm: SearchViewModel = koinViewModel(),
) {
    val global = koinInject<GlobalStore>()
    DisposableEffect(vm, query, searchType) {
        vm.mounted(query, searchType)
        onDispose {
            vm.dispose()
        }
    }

    Column {
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
                        selected = vm.searchType == SearchViewModel.SearchType.SONG,
                        onClick = { vm.updateSearchType(SearchViewModel.SearchType.SONG) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text("歌曲") }
                    )
                    SegmentedButton(
                        selected = vm.searchType == SearchViewModel.SearchType.USER,
                        onClick = { vm.updateSearchType(SearchViewModel.SearchType.USER) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text("用户") }
                    )
                }
            }
            item {
                val data = if (vm.searchType == SearchViewModel.SearchType.SONG) vm.songData else vm.userData
                if (data.isEmpty()) Box(
                    Modifier.fillMaxWidth().padding(vertical = 128.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("空空如也")
                }
            }
            if (vm.searchType == SearchViewModel.SearchType.SONG) itemsIndexed(
                items = vm.songData,
                key = { _, item -> item.id },
                contentType = { _, _ -> "song" }
            ) { _, item ->
                SearchSongItem(item, Modifier.widthIn(min = 300.dp).padding(vertical = 8.dp), {
                    global.player.insertToQueue(item.displayId, true, false)
                })
            }

            if (vm.searchType == SearchViewModel.SearchType.USER) itemsIndexed(
                items = vm.userData,
                key = { _, item -> item.uid },
                contentType = { _, _ -> "user" }
            ) { _, item ->
                SearchUserItem(
                    name = item.username,
                    avatarUrl = item.avatarUrl,
                    onClick = { global.nav.push(Route.Root.PublicUserSpace(item.uid)) },
                    modifier = Modifier.wrapContentWidth().padding(vertical = 12.dp)
                )
            }
        }
    }
}

