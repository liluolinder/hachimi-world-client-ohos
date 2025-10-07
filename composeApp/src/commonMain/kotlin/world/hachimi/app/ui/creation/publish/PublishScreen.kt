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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.PublishViewModel
import world.hachimi.app.ui.creation.publish.components.FormItem
import world.hachimi.app.ui.creation.publish.components.TagEdit
import world.hachimi.app.util.formatSongDuration
import kotlin.time.Duration.Companion.seconds

@Composable
fun PublishScreen(
    vm: PublishViewModel = koinViewModel()
) {
    val global = koinInject<GlobalStore>()
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier.fillMaxWidth().wrapContentWidth().widthIn(max = 700.dp).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("发布作品", style = MaterialTheme.typography.headlineSmall)

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
                subtitle = { Text("填写一个您认为适合永久流传的纯文字标题。如跳楼基、野哈飞舞之类的与原曲标题关联性强的纯文字标题。请不要在标题中添加标签、Emoji等复杂内容。请不要使用标题来引流，后续可能会做专门用于推荐的标题。") }
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
                subtitle = { Text("可选。使用一句话描述。你可以在后面填写原作信息，副标题中无需说明原作") }
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
                subtitle = { Text("使用标签描述你的曲风类型（如古典、流行、J-Pop、ACG、R&B）、创作类型（如原教旨、原曲不使用）。不建议添加过多的标签。若只有英文请按照每单词首字母大写空格隔开，或使用行业标准写法。请勿使用符号和 Emoji") }
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
                    Text("建议使用LRC格式的滚动歌词", modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        getPlatform().openUrl("https://lrc-maker.github.io/")
                    }) {
                        Text("制作工具")
                    }
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = vm.lyricsType == 0, onClick = { vm.lyricsType = 0 })
                    Text("LRC歌词", style = MaterialTheme.typography.labelLarge)

                    RadioButton(selected = vm.lyricsType == 1, onClick = { vm.lyricsType = 1 })
                    Text("文本歌词", style = MaterialTheme.typography.labelLarge)

                    RadioButton(selected = vm.lyricsType == 2, onClick = { vm.lyricsType = 2 })
                    Text("不填写", style = MaterialTheme.typography.labelLarge)
                }

                if (vm.lyricsType == 2) {
                    Text("强烈建议至少使用文本歌词", color = MaterialTheme.colorScheme.error)
                }

                if (vm.lyricsType != 2) OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.lyrics,
                    onValueChange = { vm.lyrics = it },
                    minLines = 8,
                    maxLines = 8,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            FormItem(
                header = { Text("创作类型") },
                subtitle = { Text("如果你的作品是对现有作品的再创作（如对《D大调卡农》的改编），请选择二创并填写原作信息。如果你的作品是对哈基米音乐的再创作（如翻唱），请选择三创") }
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

            if (vm.creationType == 0) {
                Text("原创指的是作词、作曲、编曲等全部原创，改编作品请勿选择原创。选择错误会被退回，请再次确认！", color = MaterialTheme.colorScheme.error)
            }

            if (vm.creationType > 0) {
                FormItem(header = { Text("原作基米ID") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.originId,
                        onValueChange = { vm.originId = it },
                        singleLine = true,
                        supportingText = { Text("如果原作是站内作品，填写 ID 即可，无需再填写标题与链接") }
                    )
                }
                FormItem(header = { Text("原作标题") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.originTitle,
                        onValueChange = { vm.originTitle = it },
                        singleLine = true,
                        supportingText = { Text("如 D大调卡农") }
                    )
                }
                FormItem(header = { Text("原作艺术家") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.originArtist,
                        onValueChange = { vm.originArtist = it },
                        singleLine = true,
                        supportingText = { Text("涉及到多位艺术家的，暂时填写主要的一位歌手即可") }
                    )
                }
                FormItem(header = { Text("原作链接") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.originLink,
                        onValueChange = { vm.originLink = it },
                        singleLine = true,
                        supportingText = { Text("建议填写，请使用 https:// 格式的链接") }
                    )
                }
            }

            if (vm.creationType > 1) {
                FormItem(header = { Text("二作 ID") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.deriveId,
                        onValueChange = { vm.deriveId = it },
                        singleLine = true,
                        supportingText = { Text("如果二作是站内作品，填写 ID 即可，则无需再填写标题与链接") }
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
                FormItem(header = { Text("二作艺术家") }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.deriveArtist,
                        onValueChange = { vm.deriveArtist = it },
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
                        Text(text=item.role, modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(text= item.name ?: "uid: ${item.uid}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)

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
                        Text(
                            modifier = Modifier.width(120.dp),
                            text = translatePlatformLabel(item.platform),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            modifier = Modifier.weight(1f),
                            text = item.url,
                            overflow = TextOverflow.MiddleEllipsis,
                            style = MaterialTheme.typography.bodySmall
                        )

                        IconButton(onClick = { vm.removeLink(index) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove")
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var platform by remember { mutableStateOf<String?>(null) }
                    var link by remember { mutableStateOf("") }
                    Box {
                        var dropdown by remember { mutableStateOf(false) }
                        TextButton(onClick = { dropdown = true }, modifier = Modifier.width(120.dp)) {
                            Text(
                                platform?.let {
                                    translatePlatformLabel(it)
                                } ?: "选择平台"
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "ArrowDropDown")
                        }
                        DropdownMenu(dropdown, onDismissRequest = { dropdown = false }) {
                            DropdownMenuItem(
                                text = { Text("B站") },
                                onClick = {
                                    platform = "bilibili"
                                    dropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("niconico") },
                                onClick = {
                                    platform = "niconico"
                                    dropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("YouTube") },
                                onClick = {
                                    platform = "youtube"
                                    dropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("抖音") },
                                onClick = {
                                    platform = "douyin"
                                    dropdown = false
                                }
                            )
                        }
                    }
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = link,
                        onValueChange = { link = it },
                        label = { Text("链接") },
                        placeholder = { Text("https://xxxxxx")},
                        singleLine = true
                    )
                    IconButton(onClick = {
                        platform?.let {
                            if (it.isNotBlank() && link.startsWith("https://")) {
                                vm.addLink(it, link)
                                platform = null
                                link = ""
                            } else {
                                global.alert("请选择平台并输入正确的链接")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }

            Button(onClick = { vm.publish() }, enabled = !vm.isOperating) {
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
                    Text(text = "站内用户")
                    RadioButton(selected = type == 1, onClick = {
                        vm.addStaffUid = ""
                        type = 1
                    })
                    Text(text = "站外用户")
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

@Stable
@Composable
private fun translatePlatformLabel(label: String): String = when (label) {
    // TODO: i18n
    "bilibili" -> "B站"
    "douyin" -> "抖音"
    "youtube" -> "YouTube"
    "niconico" -> "niconico"
    else -> label
}