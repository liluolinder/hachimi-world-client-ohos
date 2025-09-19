package world.hachimi.app.ui.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.hachimi.app.ui.component.Logo
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun FormCard(
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(modifier.defaultMinSize(minWidth = 280.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp)) {
            Logo(Modifier.align(Alignment.End))
            Spacer(Modifier.height(24.dp))
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.displayMedium) {
                title()
            }
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium.copy(color = LocalContentColor.current.copy(0.7f))) {
                Spacer(Modifier.height(8.dp))
                subtitle()
            }
            Spacer(Modifier.height(24.dp))
            content()
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        FormCard(
            title = { Text("成为神人") },
            subtitle = { Text("验证码已发送到您的邮箱") }
        ) {

        }
    }
}