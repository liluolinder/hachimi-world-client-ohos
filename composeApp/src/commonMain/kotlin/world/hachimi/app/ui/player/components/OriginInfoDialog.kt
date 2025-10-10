package world.hachimi.app.ui.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import world.hachimi.app.getPlatform
import world.hachimi.app.util.isValidHttpsUrl

@Composable
fun OriginInfoDialog(
    onDismissRequest: () -> Unit,
    title: String?,
    artist: String?,
    url: String?
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("原作信息") },
        text = {
            val isValidUrl = remember(url) {
                url != null && isValidHttpsUrl(url)
            }
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("标题：${title ?: "未知"}")
                    artist?.let {
                        Text("作者：${it}")
                    }
                    url?.let {
                        Row {
                            Text("链接：")
                            Text(
                                modifier = Modifier.clickable(
                                    enabled = isValidUrl,
                                    onClick = {
                                        getPlatform().openUrl(it)
                                    }
                                ),
                                text = it,
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