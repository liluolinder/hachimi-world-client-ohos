package world.hachimi.app.ui.creation.publish

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.PublishViewModel
import world.hachimi.app.ui.creation.publish.components.FormItem
import world.hachimi.app.ui.creation.publish.components.TagEdit
import world.hachimi.app.ui.root.component.formatSongDuration
import kotlin.time.Duration.Companion.seconds

@Composable
fun PublishScreen(
    vm: PublishViewModel = koinViewModel()
) {
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier.fillMaxWidth().wrapContentWidth().widthIn(max = 700.dp).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("发布作品", style = MaterialTheme.typography.headlineSmall)

            vm.error?.let {
                Text(it)
            }

            FormItem(header = { Text("上传音频") }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(onClick = { vm.setAudioFile() }, enabled = !vm.audioUploading) {
                        Text("选择文件")
                    }

                    if (vm.audioUploading) {
                        if (vm.audioUploadProgress == 0f || vm.audioUploadProgress == 1f) CircularProgressIndicator()
                        else CircularProgressIndicator(progress = { vm.audioUploadProgress })
                    }

                    if (vm.audioUploaded) {
                        Text(
                            text = "${vm.audioFileName}\n${formatSongDuration(vm.audioDurationSecs.seconds)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            FormItem(header = { Text("设置封面") }) {
                Card(
                    modifier = Modifier.size(200.dp),
                    onClick = { vm.setCoverImage() },
                    enabled = !vm.coverImageUploading
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = vm.coverImage,
                            contentDescription = "Cover Image",
                            modifier = Modifier.fillMaxSize(),
                            filterQuality = FilterQuality.Medium,
                            contentScale = ContentScale.Crop
                        )
                        if (vm.coverImageUploading) {
                            if (vm.coverImageUploadProgress == 0f || vm.coverImageUploadProgress == 1f) CircularProgressIndicator()
                            else CircularProgressIndicator(progress = { vm.coverImageUploadProgress })
                        }
                    }
                }
            }

            FormItem(
                header = { Text("标题") },
                subtitle = { Text("建议将作品标题设定为文本，不建议使用空格隔断、Emoji、符号等方式增强视觉效果或引流") }
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.title,
                    onValueChange = { vm.title = it },
                    singleLine = true
                )
            }

            FormItem(
                header = { Text("副标题") },
                subtitle = { Text("可选。副标题可以是对标题的补充、事件的补充，或是一句口号。由于你可以在后面填写原作信息，副标题中无需说明原作") }
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.subtitle,
                    onValueChange = { vm.subtitle = it },
                    singleLine = true
                )
            }

            FormItem(
                header = { Text("标签") },
                subtitle = { Text("使用标签描述你的曲风类型（如古典、流行）、创作类型（如原教旨、原曲不使用）。不建议添加过多的标签") }
            ) {
                TagEdit(vm)
            }

            FormItem(
                header = { Text("简介") },
                subtitle = { Text("介绍一下你的作品，编写一段故事，或是描述一下你的创作历程吧") }
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.description,
                    onValueChange = { vm.description = it }
                )
            }

            FormItem(
                header = { Text("歌词") },
                subtitle = {
                    Text("为了良好的用户体验，请使用 LRC 格式的歌词，暂不支持 LRC 歌词元数据")
                }
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.lyrics,
                    onValueChange = { vm.lyrics = it },
                    minLines = 8,
                    maxLines = 8
                )
            }

            FormItem(
                header = { Text("创作类型") },
                subtitle = { Text("如果你的作品是对现有作品的再创作（如对《D大调卡农》的改编），请选择二创填写原作信息。如果你的作品是对哈基米音乐的再创作（如翻唱），请选择三创") }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = vm.creationType == 0, onClick = { vm.creationType = 0 })
                    Text("原创", style = MaterialTheme.typography.labelLarge)

                    RadioButton(selected = vm.creationType == 1, onClick = { vm.creationType = 1 })
                    Text("二创", style = MaterialTheme.typography.labelLarge)

                    RadioButton(selected = vm.creationType == 2, onClick = { vm.creationType = 2 })
                    Text("三创", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (vm.creationType > 0) {
                FormItem(header = { Text("原作基米ID") }) {
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

            FormItem(
                header = {
                    Text("制作团队")
                    IconButton(onClick = { vm.addStaff() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                },
                subtitle = { Text("如果该作品的制作者不止一人，请在此添加并选择角色（如混音、编曲）。你可以选择站内用户，也可以仅填写他的名字") }
            ) {
                vm.staffs.fastForEachIndexed { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("角色: ${item.role}")
                        Text("名称: ${item.name}")
                        Text("UID: ${item.uid}")
                        IconButton(onClick = { vm.removeStaff(index) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove")
                        }
                    }
                }
            }

            FormItem(
                header = { Text("外部链接") },
                subtitle = { Text("如果你的作品已发表在其他平台上，请在此添加链接") }
            ) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var platform by remember { mutableStateOf("") }
                    var link by remember { mutableStateOf("") }
                    OutlinedTextField(
                        modifier = Modifier.width(180.dp),
                        value = platform,
                        onValueChange = { platform = it },
                        label = { Text("平台") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
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

    AddStaffDialog(vm)
}

@Composable
private fun AddStaffDialog(vm: PublishViewModel) {
    if (vm.showAddStaffDialog) AlertDialog(
        onDismissRequest = { vm.cancelAddStaff() },
        title = {
            Text("添加成员")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                var type by remember { mutableStateOf(0) }

                OutlinedTextField(
                    value = vm.addStaffRole,
                    onValueChange = { vm.addStaffRole = it },
                    label = { Text("角色") },
                    singleLine = true,
                    supportingText = {
                        Text("如：编曲、作词、混音、吉他等")
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(selected = type == 0, onClick = { type = 0 })
                    Text(text = "已注册用户")
                    RadioButton(selected = type == 1, onClick = {
                        vm.addStaffUid = ""
                        type = 1
                    })
                    Text(text = "未注册用户")
                }

                if (type == 0) OutlinedTextField(
                    value = vm.addStaffUid,
                    onValueChange = { vm.addStaffUid = it },
                    label = { Text("UID") },
                    singleLine = true
                )

                if (type == 1) OutlinedTextField(
                    value = vm.addStaffName,
                    onValueChange = { vm.addStaffName = it },
                    label = { Text("名称") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmAddStaff() },
                enabled = !vm.addStaffOperating && (vm.addStaffRole.isNotBlank() && (vm.addStaffUid.isNotBlank() || vm.addStaffName.isNotBlank()))
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.cancelAddStaff() }) {
                Text("取消")
            }
        }
    )
}