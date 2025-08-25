package world.hachimi.app.ui.creation.artwork

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.MyArtworkViewModel
import world.hachimi.app.nav.Route

@Composable
fun MyArtworkScreen(
    vm: MyArtworkViewModel = koinViewModel()
) {
    val global = koinInject<GlobalStore>()
    Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = "我的作品",
                style = MaterialTheme.typography.titleLarge
            )

            Button(onClick = {
                global.nav.push(Route.Root.CreationCenter.Publish)
            }) {
                Text("发布作品")
            }
        }

    }
}