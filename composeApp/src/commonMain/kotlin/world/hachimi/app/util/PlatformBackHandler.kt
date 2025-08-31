package world.hachimi.app.util

import androidx.compose.runtime.Composable

/**
 * This might be removed in the future. We should find a way to support predict-back if possible.
 */
@Composable
expect fun PlatformBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
)