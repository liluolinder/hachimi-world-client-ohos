package world.hachimi.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("推荐音乐")

        if (vm.isLoading) {
            CircularProgressIndicator()
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        }
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
        Column(Modifier.padding(12.dp)) {
            AsyncImage(coverUrl, null, Modifier.fillMaxWidth().aspectRatio(1f))
            Text(title)
            Text(author)

            Row {
                tags.forEach { tag ->
                    Surface(Modifier.padding(4.dp)) {
                        Text(tag)
                    }
                }
            }
        }
    }
}