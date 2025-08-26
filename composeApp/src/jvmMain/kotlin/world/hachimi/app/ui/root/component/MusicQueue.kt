package world.hachimi.app.ui.root.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore

@Composable
fun MusicQueue(
    onClose: () -> Unit,
) {
    val global = koinInject<GlobalStore>()

    Card(
        modifier = Modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = CardDefaults.elevatedShape,
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(Modifier.padding(24.dp).width(400.dp).height(600.dp)) {
            Row {
                Text(text = "播放队列 (${global.musicQueue.size})", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            global.musicQueue.fastForEachIndexed { index, item ->
                val isPlaying = item.songId == global.playerState.songId

                val interactionSource = remember { MutableInteractionSource() }
                val hovered by interactionSource.collectIsHoveredAsState();

                CompositionLocalProvider(
                    LocalContentColor provides if (isPlaying) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        LocalContentColor.current
                    }
                ) {
                    Row(
                        modifier = Modifier.height(42.dp).clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current
                        ) { global.playSongInQueue(item.songId) },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = index.toString())
                        Text(item.name)
                        Text(item.artist)
                        Spacer(Modifier.weight(1f))
                        Text(formatSongDuration(item.duration), style = MaterialTheme.typography.bodySmall)
                        if (hovered) IconButton(
                            onClick = { global.removeFromQueue(item.songId) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove from playlist")
                        }
                    }
                }
            }
        }
    }
}
