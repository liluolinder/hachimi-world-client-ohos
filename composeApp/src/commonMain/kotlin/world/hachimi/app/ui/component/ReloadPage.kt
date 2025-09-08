package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun ReloadPage(
    onReloadClick: () -> Unit,
    message: String? = null,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    Box(modifier, Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("出错了")
            Spacer(Modifier.height(12.dp))
            Button(onReloadClick) {
                Text("重试")
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        ReloadPage(onReloadClick = {})
    }
}