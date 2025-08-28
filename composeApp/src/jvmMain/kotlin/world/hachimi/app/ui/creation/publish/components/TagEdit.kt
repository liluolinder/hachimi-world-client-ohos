package world.hachimi.app.ui.creation.publish.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed

@Composable
fun TagEdit(
    tags: List<String>,
    value: String,
    onValueChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onRemoveClick: (index: Int) -> Unit,
) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.fastForEachIndexed { index, item ->
                AssistChip(
                    label = { Text(item) },
                    trailingIcon = {
//                        IconButton(onClick = { onRemoveClick(index) }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove")
//                        }
                    },
                    onClick = {
                        onRemoveClick(index)
                    }
                )
            }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardActions = KeyboardActions(onDone = { onAddClick() }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
    }
}