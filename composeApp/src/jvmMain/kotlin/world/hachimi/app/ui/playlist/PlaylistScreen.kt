package world.hachimi.app.ui.playlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PlaylistViewModel

@Composable
fun PlaylistScreen(vm: PlaylistViewModel = koinViewModel()) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose {
            vm.dispose()
        }
    }
    val global = koinInject<GlobalStore>()
    Column(
        modifier = Modifier.fillMaxSize().wrapContentSize().widthIn(max = 1000.dp).padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(text = "我的歌单", style = MaterialTheme.typography.titleLarge)
        if (vm.playlistIsLoading) {
            CircularProgressIndicator()
        }
        vm.playlists.fastForEach { playlist ->

        }
    }
}

@Composable
private fun PlaylistItem() {

}