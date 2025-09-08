package world.hachimi.app.ui.creation.artwork

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.Instant
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.MyArtworkViewModel
import world.hachimi.app.nav.Route
import world.hachimi.app.util.formatTime

@Composable
fun MyArtworkScreen(
    vm: MyArtworkViewModel = koinViewModel()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }

    val global = koinInject<GlobalStore>()
    val scrollState = rememberLazyListState()

    LaunchedEffect(scrollState.canScrollForward) {
        if (!scrollState.canScrollForward && !vm.loading) {
            vm.loadMore()
        }
    }

    AnimatedContent(vm.initializeStatus, modifier = Modifier.fillMaxSize()) {
        when(it) {
            MyArtworkViewModel.InitializeStatus.INIT -> Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            MyArtworkViewModel.InitializeStatus.FAILED -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("出错了")
                    Button(onClick = { vm.refresh() }) {
                        Text("重试")
                    }
                }
            }

            MyArtworkViewModel.InitializeStatus.LOADED -> LazyColumn(
                state = scrollState,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "我的作品 (${vm.total})",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Button(onClick = {
                            global.nav.push(Route.Root.CreationCenter.Publish)
                        }) {
                            Text("发布作品")
                        }
                    }
                }
                itemsIndexed(vm.items, key = { _, item -> item.reviewId }) { index, item ->
                    ArtworkItem(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        coverUrl = item.coverUrl,
                        title = item.title,
                        subtitle = item.subtitle,
                        submitTime = item.submitTime,
                        status = when (item.status) {
                            PublishModule.SongPublishReviewBrief.STATUS_PENDING -> "待审核"
                            PublishModule.SongPublishReviewBrief.STATUS_APPROVED -> "通过"
                            PublishModule.SongPublishReviewBrief.STATUS_REJECTED -> "驳回"
                            else -> "未知"
                        },
                        onEditClick = {
                            // TODO
                        }
                    )
                }
                item {
                    if (vm.noMoreData) Box(Modifier.fillMaxWidth(), Alignment.Center) {
                        Text(
                            text = "没有更多了",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                item {
                    if (vm.loading) Box(Modifier.fillMaxWidth(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtworkItem(
    coverUrl: String,
    title: String,
    subtitle: String,
    submitTime: Instant,
    status: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(Modifier.size(42.dp), MaterialTheme.shapes.small, LocalContentColor.current.copy(0.12f)) {
            AsyncImage(
                modifier = Modifier.size(42.dp).clip(MaterialTheme.shapes.small),
                model = coverUrl,
                contentDescription = "Cover Image",
                contentScale = ContentScale.Crop
            )
        }

        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "提交时间", style = MaterialTheme.typography.bodySmall)
            Text(text = formatTime(submitTime, distance = true, precise = false), style = MaterialTheme.typography.bodySmall)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "状态", style = MaterialTheme.typography.bodySmall)
            Text(text = status, style = MaterialTheme.typography.bodySmall)
        }

        /*Box {
            var expanded by remember { mutableStateOf(false) }
            TextButton(onClick = { expanded = true }) {
                Text("操作")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("编辑") }, onClick = onEditClick)
            }
        }*/
    }
}