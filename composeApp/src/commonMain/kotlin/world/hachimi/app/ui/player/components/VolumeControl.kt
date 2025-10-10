package world.hachimi.app.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = when {
                volume <= 0f -> Icons.AutoMirrored.Filled.VolumeMute
                volume <= 0.5f -> Icons.AutoMirrored.Filled.VolumeDown
                volume > 0.5f -> Icons.AutoMirrored.Filled.VolumeUp
                else -> error("unreachable")
            },
            contentDescription = "Volume Control"
        )
        Spacer(Modifier.width(4.dp))
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.width(100.dp).height(6.dp),
            thumb = {},
            track = { sliderState ->
                Box(Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)) {
                    Box(Modifier.fillMaxWidth(sliderState.value).height(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                }
            }
        )
    }
}