package world.hachimi.app.util

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.fillMaxWithLimit(maxWidth: Dp = 1000.dp): Modifier
    = this.fillMaxSize().wrapContentWidth().widthIn(max = maxWidth)