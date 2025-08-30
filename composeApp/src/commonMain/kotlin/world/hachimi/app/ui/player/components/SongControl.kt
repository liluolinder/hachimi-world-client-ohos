package world.hachimi.app.ui.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SongControl(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isLoading: Boolean,
    loadingProgress: Float,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.Default.SkipPrevious, "Skip Previous")
        }
        IconButton(onClick = onPlayPauseClick, colors = IconButtonDefaults.filledIconButtonColors()) {
            if (isLoading) {
                if (loadingProgress == 0f) CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = LocalContentColor.current,
                    strokeWidth = 2.dp
                ) else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = LocalContentColor.current,
                        strokeWidth = 2.dp,
                        progress = { loadingProgress }
                    )
                }
            } else {
                if (isPlaying) {
                    Icon(Icons.Default.Pause, "Pause")
                } else {
                    Icon(Icons.Default.PlayArrow, "Play")
                }
            }
        }
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.SkipNext, "Skip Next")
        }
    }
}