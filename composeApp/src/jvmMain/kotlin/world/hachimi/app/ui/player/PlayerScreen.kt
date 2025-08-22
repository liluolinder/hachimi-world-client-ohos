package world.hachimi.app.ui.player

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import world.hachimi.app.model.GlobalStore
import kotlin.math.roundToInt

@Composable
fun PlayerScreen() {
    Surface {
        Box {
            Row(Modifier.fillMaxSize().padding(32.dp)) {
                Column(Modifier.fillMaxHeight().weight(1f), verticalArrangement = Arrangement.Center) {
                    Column(Modifier.align(Alignment.End).padding(48.dp)) {
                        BoxWithConstraints(Modifier.wrapContentSize()) {
                            val size = min(maxHeight * 0.7f, maxWidth)
                            Card(
                                modifier = Modifier.size(size),
                                elevation = CardDefaults.cardElevation(12.dp)
                            ) {

                            }
                        }


                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "\uD83C\uDFB5曼波 \uD835\uDC75\uD835\uDC90 \uD835\uDC74\uD835\uDC90\uD835\uDC93\uD835\uDC86\uD83C\uDFB5不再曼波"
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text ="还给我神ID",
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalContentColor.current.copy(0.6f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "基米ID：JM-CCB1145\n" +
                                    "编曲：还给我神ID\n" +
                                    "混音：还给我神ID\n" +
                                    "作曲：还给我神ID\n" +
                                    "作词：还给我神ID\u2028原作：Normal No More\n" +
                                    "外部链接：BiliBili",
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalContentColor.current.copy(0.7f)
                        )
                    }
                }

                Spacer(Modifier.width(64.dp))

                var currentLine by remember { mutableStateOf(0) }
                val lines = remember { testLyric.lines() }
                LaunchedEffect(Unit) {
                    while (isActive) {
                        if (currentLine < lines.size - 1) {
                            currentLine += 1
                        } else {
                            currentLine = 0
                        }
                        delay(3000)
                    }
                }
                Lyrics(
                    currentLine,
                    lines,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                )
            }

            IconButton(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                onClick = { GlobalStore.shrinkPlayer() }
            ) {
                Icon(Icons.Default.CloseFullscreen, "Shrink")
            }
        }
    }
}

@Composable
fun Lyrics(
    currentLine: Int,
    lines: List<String>,
    modifier: Modifier
) {
    BoxWithConstraints(modifier) {
        val lazyListState = rememberLazyListState()
        val scrollOffset = with(LocalDensity.current) {
            32.dp.roundToPx()
        }
        LaunchedEffect(currentLine) {
            lazyListState.animateScrollToItem(currentLine, scrollOffset = -(constraints.maxHeight * 0.3).roundToInt())
        }

        LazyColumn(Modifier.fillMaxSize(), lazyListState, contentPadding = PaddingValues(
            bottom = maxHeight,
        )) {
            itemsIndexed(lines) { index, line ->
                val current = index == currentLine
                val transition = updateTransition(current)

                val scale by transition.animateFloat { if (it) 1f else 0.7f }
                val alpha by transition.animateFloat { if (it) 1f else 0.9f }

                Text(
                    modifier = Modifier.padding(vertical = 12.dp).graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    },
                    text = line,
                    color = LocalContentColor.current.copy(alpha = alpha),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

val testLyric = """哈基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸
哈基基米多 娜娜美撸多 阿西嘎哈呀
曼波曼波 哦马子gi曼波
砸不砸不 耶哒耶哒漫步
哈基米哦南北绿豆
阿西嘎哈椰果奶龙
曼波曼波 哦马子gi曼波
砸不砸不 耶哒耶哒漫步
哈基米哦南北绿豆
阿西嘎哈椰果奶龙～曼波！
哈基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸
哈基基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸（漫步）
哈基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸
哈基基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸
哦马子gi 曼波
咋不咋不 曼波
哦马子gi 曼波
耶哒耶哒 曼波
哈基米哦南北路
阿西嘎哈椰果奶龙
娜美撸多哈基米绷
阿西噶哈路哈呀
曼波曼波 哦马子gi曼波
砸不砸不 耶哒耶哒漫步
哈基米哦南北绿豆
阿西噶哈椰果奶龙
曼波曼波 哦马子gi曼波
砸不砸不 耶哒耶哒漫步
哈基米哦南北绿豆
阿西噶哈椰果奶龙～曼波！
哈基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸
哈基基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸（漫步）
哈基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸
哈基基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸
曼波曼波 哦马子gi 哈呀哈基米
曼波 曼波 曼波 哦马子gi 阿西噶呀阿西
哈基米哦南北路
阿西嘎哈椰果奶龙
娜美撸多哈基米绷
阿西嘎哈路哈呀
曼波曼波 哦马子gi曼波
砸不砸不 耶哒耶哒漫步
哈基米哦南北绿豆
阿西嘎哈椰果奶龙
曼波曼波 哦马子gi曼波
砸不砸不 耶哒耶哒漫步
哈基米哦南北绿豆
阿西嘎哈椰果奶龙～曼波！
哈基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸
哈基基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸（漫步）
哈基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸
哈基基米多 娜娜美撸多 阿西噶哈呀咕咕奈撸"""