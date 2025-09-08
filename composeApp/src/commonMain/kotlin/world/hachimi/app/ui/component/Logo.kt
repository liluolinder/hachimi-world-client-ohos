package world.hachimi.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.icon_3d_512x
import hachimiworld.composeapp.generated.resources.logo_text
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.vectorResource
import world.hachimi.app.ui.LocalDarkMode

@Composable
fun Logo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(56.dp),
            bitmap = imageResource(Res.drawable.icon_3d_512x),
            contentDescription = "基米天堂 Icon",
            filterQuality = FilterQuality.High,
        )
        Image(
            imageVector = vectorResource(Res.drawable.logo_text),
            contentDescription = "基米天堂",
            colorFilter = ColorFilter.tint(
                if (LocalDarkMode.current) Color(0xFFE8E0D4)
                else Color(0xFF4F432F)
            )
        )
    }
}