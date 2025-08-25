package world.hachimi.app.ui.creation.artwork

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.MyArtworkViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.root.component.formatSongDuration
import world.hachimi.app.util.formatTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun MyArtworkScreen(
    vm: MyArtworkViewModel = koinViewModel()
) {
    val global = koinInject<GlobalStore>()
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
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
        item {
            ArtworkItem(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                coverUrl = "",
                title = "测试作品",
                subtitle = "测试副标题",
                duration = 256.seconds,
                releaseTime = remember { Clock.System.now() },
                likeCount = 200000,
                status = "已发布",
                onEditClick = {
                    // TODO
                }
            )
        }
    }
}

@Composable
private fun ArtworkItem(
    coverUrl: String,
    title: String,
    subtitle: String,
    duration: Duration,
    likeCount: Int,
    releaseTime: Instant,
    status: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        AsyncImage(
            model = coverUrl, null,
            modifier = Modifier.size(42.dp).clip(MaterialTheme.shapes.small),
            placeholder = ColorPainter(Color.LightGray),
            error = ColorPainter(Color.LightGray),
            fallback = ColorPainter(Color.LightGray),
        )

        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text = formatSongDuration(duration), style = MaterialTheme.typography.bodySmall)
            Text(text = likeCount.toString(), style = MaterialTheme.typography.bodySmall)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "发布时间", style = MaterialTheme.typography.bodySmall)
            Text(text = formatTime(releaseTime), style = MaterialTheme.typography.bodySmall)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "状态", style = MaterialTheme.typography.bodySmall)
            Text(text = status, style = MaterialTheme.typography.bodySmall)
        }

        Box {
            var expanded by remember { mutableStateOf(false) }
            TextButton(onClick = { expanded = true }) {
                Text("操作")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("编辑") }, onClick = onEditClick)
            }
        }
    }
}