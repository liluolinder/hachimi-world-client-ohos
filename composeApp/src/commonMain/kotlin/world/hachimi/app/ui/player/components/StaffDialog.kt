package world.hachimi.app.ui.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.hachimi.app.api.module.SongModule

@Composable
fun StaffDialog(
    onDismissRequest: () -> Unit,
    uploaderUid: Long,
    uploaderName: String,
    crew: List<SongModule.SongProductionCrew>,
    onNavToAuthorClick: (Long) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("神人团队")
        },
        text = {
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserItem(
                        role = "作者",
                        uid = uploaderUid,
                        name = uploaderName,
                        onClick = { onNavToAuthorClick(uploaderUid) }
                    )
                    crew.forEach { item ->
                        UserItem(
                            role = item.role,
                            uid = item.uid,
                            name = item.personName,
                            onClick = { item.uid?.let { onNavToAuthorClick(it) } }
                        )
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

@Composable
private fun UserItem(
    role: String,
    uid: Long?,
    name: String?,
    onClick: () -> Unit,
) {
    Text(text = role, style = MaterialTheme.typography.labelMedium)
    Text(
        modifier = Modifier.clickable(
            enabled = uid != null,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        text = name ?: "未知",
    )
}