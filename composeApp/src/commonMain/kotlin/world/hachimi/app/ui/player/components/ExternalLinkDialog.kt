package world.hachimi.app.ui.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.getPlatform
import world.hachimi.app.ui.creation.publish.translatePlatformLabel
import world.hachimi.app.util.isValidHttpsUrl

@Composable
fun ExternalLinkDialog(
    onDismissRequest: () -> Unit,
    links: List<SongModule.ExternalLink>
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("MV") },
        text = {
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    links.forEach { link ->
                        Text("平台：${translatePlatformLabel(link.platform)}")
                        Row {
                            Text("链接：")
                            val isValidUrl = remember(link.url) { isValidHttpsUrl(link.url) }
                            Text(
                                modifier = Modifier.clickable(
                                    enabled = isValidUrl,
                                    onClick = {
                                        getPlatform().openUrl(link.url)
                                    }
                                ).widthIn(max = 320.dp),
                                text = link.url,
                                textDecoration = if (isValidUrl) TextDecoration.Underline else TextDecoration.None
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("确定")
            }
        }
    )
}