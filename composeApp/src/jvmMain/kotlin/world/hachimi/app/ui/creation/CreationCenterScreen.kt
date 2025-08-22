package world.hachimi.app.ui.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.CreationCenterViewModel
import world.hachimi.app.util.formatDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun CreationCenterScreen(
    vm: CreationCenterViewModel = koinViewModel()
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("发布作品", style = MaterialTheme.typography.headlineSmall)

        vm.error?.let {
            Text(it)
        }

        FormItem(header = { Text("上传音频") }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    vm.setAudioFile()
                }) {
                    Text("上传文件")
                }
                if (vm.audioUploading) {
                    LinearProgressIndicator(progress = { vm.audioUploadProgress })
                }
                if (vm.audioUploaded) {
                    Text("${vm.audioFileName} ${formatDuration(vm.audioDurationSecs.seconds, true)}")
                }
            }
        }

        FormItem(header = { Text("设置封面") }) {
            Card(
                modifier = Modifier.size(200.dp),
                onClick = { vm.setCoverImage() }
            ) {
                AsyncImage(
                    model = vm.coverImage,
                    contentDescription = "Cover Image",
                    modifier = Modifier.fillMaxSize(),
                    filterQuality = FilterQuality.Medium,
                    contentScale = ContentScale.Crop
                )
            }
            if (vm.coverImageUploading) {
                LinearProgressIndicator(
                    progress = { vm.coverImageUploadProgress },
                )
            }
        }

        FormItem(header = { Text("标题") }) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = vm.title,
                onValueChange = { vm.title = it },
                singleLine = true
            )
        }

        FormItem(header = { Text("副标题") }) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = vm.subtitle,
                onValueChange = { vm.subtitle = it },
                singleLine = true
            )
        }

        FormItem(header = { Text("标签") }) {
            val (value, setValue) = remember { mutableStateOf("") }
            TagEdit(
                tags = vm.tags,
                value = value,
                onValueChange = setValue,
                onAddClick = {
                    if (value.isNotBlank()) {
                        vm.addTag(value)
                        setValue("")
                    }
                },
                onRemoveClick = { vm.removeTag(it) }
            )
        }

        FormItem(header = { Text("简介") }) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = vm.description,
                onValueChange = { vm.description = it }
            )
        }

        FormItem(header = { Text("歌词（LRC格式）") }) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = vm.lyrics,
                onValueChange = { vm.lyrics = it }
            )
        }

        FormItem(header = { Text("创作类型") }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = vm.creationType == 0, onClick = { vm.creationType = 0 })
                Text("原创")

                RadioButton(selected = vm.creationType == 1, onClick = { vm.creationType = 1 })
                Text("二创")

                RadioButton(selected = vm.creationType == 2, onClick = { vm.creationType = 2 })
                Text("三创")
            }
        }

        if (vm.creationType > 0) {
            FormItem(header = { Text("原作 ID") }) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.originId,
                    onValueChange = { vm.originId = it },
                    singleLine = true
                )
            }
            FormItem(header = { Text("原作标题") }) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.originTitle,
                    onValueChange = { vm.originTitle = it },
                    singleLine = true
                )
            }
            FormItem(header = { Text("原作链接") }) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.originLink,
                    onValueChange = { vm.originLink = it },
                    singleLine = true
                )
            }
        }

        if (vm.creationType > 1) {
            FormItem(header = { Text("二作 ID") }) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.deriveId,
                    onValueChange = { vm.deriveId = it },
                    singleLine = true
                )
            }
            FormItem(header = { Text("二作标题") }) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.deriveTitle,
                    onValueChange = { vm.deriveTitle = it },
                    singleLine = true
                )
            }
            FormItem(header = { Text("二作链接") }) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.deriveLink,
                    onValueChange = { vm.deriveLink = it },
                    singleLine = true
                )
            }
        }

        FormItem(header = {
            Text("制作团队")
        }) {
            vm.crews.fastForEachIndexed { index, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("角色: ${item.role}")
                    Text("名称: ${item.name}")
                    Text("UID: ${item.uid}")
                    IconButton(onClick = { vm.removeCrewMember(index) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Remove")
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var uid by remember { mutableStateOf("") }
                var name by remember { mutableStateOf("") }
                var role by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = uid,
                    onValueChange = { uid = it },
                    label = { Text("UID") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("角色") },
                    singleLine = true
                )
                IconButton(onClick = {
                    if (uid.isNotBlank() || (name.isNotBlank() && role.isNotBlank())) {
                        vm.addCrewMember(uid, name, role)
                        uid = ""
                        name = ""
                        role = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }

        FormItem(header = {
            Text("外部链接")
        }) {
            vm.externalLinks.fastForEachIndexed { index, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("平台: ${item.platform}")
                    Text("链接: ${item.url}")

                    IconButton(onClick = { vm.removeLink(index) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Remove")
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var platform by remember { mutableStateOf("") }
                var link by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = platform,
                    onValueChange = { platform = it },
                    label = { Text("平台") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("URL") },
                    singleLine = true
                )
                IconButton(onClick = {
                    if (platform.isNotBlank() && link.isNotBlank()) {
                        vm.addLink(platform, link)
                        platform = ""
                        link = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }

        Button(onClick = { vm.publish() }, enabled = vm.publishEnabled) {
            Text("提交作品")
        }
    }

    if (vm.showSuccessDialog) AlertDialog(
        onDismissRequest = { vm.closeDialog() },
        title = {
            Text("作品发布成功")
        },
        text = {
            Text("你的作品编号为：${vm.publishedSongId}")
        },
        confirmButton = {
            TextButton(onClick = { vm.closeDialog() }) {
                Text("确定")
            }
        }
    )
}

@Composable
fun FormItem(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier) {
        CompositionLocalProvider(
            value = LocalTextStyle provides MaterialTheme.typography.bodyMedium,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                header()
            }
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

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