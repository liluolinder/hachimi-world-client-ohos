package world.hachimi.app.ui.player

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import kotlin.math.roundToInt

@Composable
fun PlayerScreen() {
    val global = koinInject<GlobalStore>()

    Surface {
        Box {
            Row(Modifier.fillMaxSize().padding(32.dp)) {
                Column(Modifier.fillMaxHeight().weight(1f), verticalArrangement = Arrangement.Center) {
                    Column(Modifier.align(Alignment.End).padding(48.dp)) {
                        BoxWithConstraints(Modifier.wrapContentSize()) {
                            val size = min(maxHeight * 0.7f, maxWidth)
                            Card(
                                modifier = Modifier.size(size),
                                elevation = CardDefaults.cardElevation(12.dp)
                            ) {
                                AsyncImage(global.playerState.songCoverUrl, null, Modifier.fillMaxSize())
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = global.playerState.songTitle,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = global.playerState.songAuthor,
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalContentColor.current.copy(0.6f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "基米ID：${global.playerState.songId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalContentColor.current.copy(0.7f)
                        )
                    }
                }

                Spacer(Modifier.width(64.dp))

                Lyrics(
                    currentLine = global.playerState.currentLyricsLine,
                    lines = global.playerState.lyricsLines,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                )
            }

            IconButton(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                onClick = { global.shrinkPlayer() }
            ) {
                Icon(Icons.Default.CloseFullscreen, "Shrink")
            }
        }
    }
}

@Composable
fun Lyrics(
    currentLine: Int,
    lines: List<String>,
    modifier: Modifier
) {
    BoxWithConstraints(modifier) {
        val lazyListState = rememberLazyListState()
        LaunchedEffect(currentLine) {
            if (currentLine == -1) {
                lazyListState.scrollToItem(0)
            } else {
                lazyListState.animateScrollToItem(currentLine, scrollOffset = -(constraints.maxHeight * 0.3).roundToInt())
            }
        }

        LazyColumn(Modifier.fillMaxSize(), lazyListState, contentPadding = PaddingValues(
            bottom = maxHeight,
        )) {
            itemsIndexed(lines) { index, line ->
                val current = index == currentLine
                val transition = updateTransition(current)

                val scale by transition.animateFloat { if (it) 1f else 0.7f }
                val alpha by transition.animateFloat { if (it) 1f else 0.9f }

                Text(
                    modifier = Modifier.padding(vertical = 12.dp).graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    },
                    text = line,
                    color = LocalContentColor.current.copy(alpha = alpha),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

