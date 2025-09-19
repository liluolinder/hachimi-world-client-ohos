package world.hachimi.app.util

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.fillMaxWithLimit(maxWidth: Dp = 1000.dp): Modifier =
    this.fillMaxSize().wrapContentWidth().widthIn(max = maxWidth)

object WindowSize {
    val COMPACT = 600.dp
    val MEDIUM = 840.dp
    val EXPANDED = 1200.dp
    val LARGE = 1600.dp
    val EXTRA_LARGE = 1920.dp
}