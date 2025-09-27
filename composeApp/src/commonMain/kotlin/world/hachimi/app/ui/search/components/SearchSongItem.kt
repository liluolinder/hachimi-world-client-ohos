package world.hachimi.app.ui.search.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.formatSongDuration
import kotlin.time.Duration.Companion.seconds

@Composable
fun SearchSongItem(
    data: SongModule.SearchSongItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier.height(80.dp),
        onClick = onClick
    ) {
        Row {
            Surface(Modifier.aspectRatio(1f).fillMaxHeight(), shape = MaterialTheme.shapes.medium) {
                AsyncImage(
                    model = data.coverArtUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            Row(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(data.title, style = MaterialTheme.typography.bodyMedium)
                    Text(data.subtitle, style = MaterialTheme.typography.bodySmall, color = LocalContentColor.current.copy(0.72f))
                    Spacer(Modifier.weight(1f))
                    Text(data.uploaderName, style = MaterialTheme.typography.labelSmall, color = LocalContentColor.current.copy(0.6f))
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = "Duration", modifier = Modifier.size(12.dp))
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = formatSongDuration(data.durationSeconds.seconds), style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Headphones, contentDescription = "Play Count", modifier = Modifier.size(12.dp))
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = data.playCount.toString(), style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
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

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        SearchSongItem(data = SongModule.SearchSongItem(
            id = 0,
            displayId = "1",
            title = "Test Song Title",
            subtitle = "This is a test subtitle",
            description = "test",
            artist = "test",
            durationSeconds = 100,
            playCount = 100,
            likeCount = 100,
            coverArtUrl = "",
            audioUrl = "",
            uploaderUid = 0,
            uploaderName = "Author"
        ))
    }
}