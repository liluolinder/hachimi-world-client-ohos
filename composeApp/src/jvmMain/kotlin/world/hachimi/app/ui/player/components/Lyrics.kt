package world.hachimi.app.ui.player.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

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
