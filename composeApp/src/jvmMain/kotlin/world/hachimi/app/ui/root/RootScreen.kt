package world.hachimi.app.ui.root

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.RootContent
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.home.HomeScreen
import world.hachimi.app.ui.creation.CreationCenterScreen
import world.hachimi.app.ui.root.component.FooterPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(content: RootContent) {
    val global = koinInject<GlobalStore>()
    Column(Modifier.fillMaxSize()) {
        Surface(Modifier.fillMaxWidth().height(68.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("基米天堂", style = MaterialTheme.typography.headlineMedium)

                Row(Modifier.weight(1f).wrapContentWidth()) {
                    var searchText by remember { mutableStateOf("") }
                    SearchBox(searchText, { searchText = it}, modifier = Modifier.widthIn(max = 400.dp))
                }

                if (global.isLoggedIn) {
                    val userInfo = global.userInfo!!
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(0.12f), CircleShape)
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(
                                text = userInfo.name,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Lv.4",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    Button(onClick = {
                        global.nav.push(Route.Auth())
                    }) {
                        Text("登录")
                    }
                    Button(onClick = {
                        global.nav.push(Route.Auth(false))
                    }) {
                        Text("注册")
                    }
                }
            }
        }

        Row(Modifier.weight(1f).fillMaxWidth()) {
            Card(Modifier.padding(start = 24.dp, top = 24.dp).width(210.dp)) {
                Column(Modifier.padding(12.dp)) {
                    NavItem("首页", selected = content == RootContent.Home, onSelectedChange = {
                        global.nav.push(Route.Root(RootContent.Home))
                    })
                    if (global.isLoggedIn) {
                        NavItem("最近点赞", selected = content == RootContent.RecentLike, onSelectedChange = {
                            global.nav.push(Route.Root(RootContent.RecentLike))
                        })
                        NavItem("最近播放", selected = content == RootContent.RecentPlay, onSelectedChange = {
                            global.nav.push(Route.Root(RootContent.RecentPlay))
                        })

                        NavItem("我的歌单", selected = content == RootContent.MyPlaylist, onSelectedChange = {
                            global.nav.push(Route.Root(RootContent.MyPlaylist))
                        })
                        NavItem("我的关注", selected = content == RootContent.MySubscribe, onSelectedChange = {
                            global.nav.push(Route.Root(RootContent.MySubscribe))
                        })

                        NavItem("创作中心", selected = content == RootContent.CreationCenter, onSelectedChange = {
                            global.nav.push(Route.Root(RootContent.CreationCenter))
                        })

                        NavItem("委员会中心", selected = content == RootContent.CommitteeCenter, onSelectedChange = {
                            global.nav.push(Route.Root(RootContent.CommitteeCenter))
                        })
                        NavItem("维护者中心", selected = content == RootContent.ContributorCenter, onSelectedChange = {
                            global.nav.push(Route.Root(RootContent.ContributorCenter))
                        })
                    }
                }
            }

            Spacer(Modifier.width(24.dp))

            Box(Modifier.weight(1f).fillMaxHeight()) {
                when (content) {
                    RootContent.Home -> HomeScreen()
                    RootContent.CommitteeCenter -> {}
                    RootContent.ContributorCenter -> {}
                    RootContent.CreationCenter -> CreationCenterScreen()
                    RootContent.MyPlaylist -> {}
                    RootContent.MySubscribe -> {}
                    RootContent.RecentLike -> {}
                    RootContent.RecentPlay -> {}
                }
            }
        }

        FooterPlayer()
    }
}

@Composable
private fun NavItem(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    if (selected) {
        Button(modifier = modifier, onClick = {}) {
            Text(label)
        }
    } else {
        TextButton(modifier = modifier, onClick = { onSelectedChange(true) }) {
            Text(label)
        }
    }
}

@Composable
private fun SearchBox(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        modifier = modifier.defaultMinSize(300.dp),
        value = searchText,
        onValueChange = onSearchTextChange,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    innerTextField()
                }
                Spacer(Modifier.width(8.dp))
                IconButton(modifier = Modifier.align(Alignment.CenterVertically), onClick = {}) {
                    Icon(Icons.Default.Search, "Search")
                }
            }
        },
        singleLine = true
    )
}