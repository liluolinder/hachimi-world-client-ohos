package world.hachimi.app.ui.userspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.UserSpaceViewModel

@Composable
fun UserSpaceScreen(vm: UserSpaceViewModel = koinViewModel()) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose {
            vm.dispose()
        }
    }
    val global = koinInject<GlobalStore>()
    Column(
        modifier = Modifier.fillMaxSize().wrapContentSize().widthIn(max = 1000.dp).padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            Text(modifier = Modifier.weight(1f), text = "神人空间", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { global.logout() }) {
                Text("退出登录")
            }
        }

        if (vm.loading) {
            CircularProgressIndicator()
        } else vm.profile?.let { profile ->
            Row(Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        modifier = Modifier.size(128.dp),
                        shape = CircleShape,
                        onClick = { vm.editAvatar() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = profile.avatarUrl,
                                contentDescription = "User Avatar",
                                modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.small),
                                filterQuality = FilterQuality.High,
                                contentScale = ContentScale.Crop
                            )

                            if (vm.avatarUploading) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        modifier = Modifier.clickable {
                            vm.editUsername()
                        },
                        text = profile.username
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when (profile.gender) {
                            0 -> "男"
                            1 -> "女"
                            else -> "保密"
                        }, style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Lv.4", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.width(24.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "UID: ${profile.uid}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "介绍", style = MaterialTheme.typography.bodyLarge)

                    Card(
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 120.dp),
                        onClick = { vm.editBio() }
                    ) {
                        Text(
                            modifier = Modifier.padding(12.dp),
                            text = profile.bio ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Text(text = "全部作品", style = MaterialTheme.typography.titleLarge)
        Card(Modifier.fillMaxWidth().height(400.dp)) {

        }

        Text(text = "创建的歌单", style = MaterialTheme.typography.titleLarge)
        Card(Modifier.fillMaxWidth().height(400.dp)) {

        }

        if (vm.showEditBio) AlertDialog(
            onDismissRequest = { vm.cancelEdit() },
            title = {
                Text("更改简介")
            },
            text = {
                TextField(value = vm.editBioValue, onValueChange = { vm.editBioValue = it })
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.confirmEditBio() },
                    enabled = !vm.operating && vm.editBioValue.isNotBlank()
                ) {
                    Text("更改")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.cancelEdit() }) {
                    Text("取消")
                }
            }
        )

        if (vm.showEditUsername) AlertDialog(
            onDismissRequest = { vm.cancelEdit() },
            title = {
                Text("更改昵称")
            },
            text = {
                TextField(value = vm.editUsernameValue, onValueChange = { vm.editUsernameValue = it })
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.confirmEditUsername() },
                    enabled = !vm.operating && vm.editUsernameValue.isNotBlank()
                ) {
                    Text("更改")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.cancelEdit() }) {
                    Text("取消")
                }
            }
        )
    }
}