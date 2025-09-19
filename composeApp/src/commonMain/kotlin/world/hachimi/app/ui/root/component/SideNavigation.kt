package world.hachimi.app.ui.root.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Route

@Composable
fun SideNavigation(
    global: GlobalStore,
    content: Route,
    onChange: (Route) -> Unit = {},
) {
    Column(Modifier.defaultMinSize(minWidth = 300.dp)) {
        NavItem(icon = Icons.Default.Home,label = "首页", selected = content == Route.Root.Home, onSelectedChange = {
            onChange(Route.Root.Home)
        })
        if (global.isLoggedIn) {
            /*NavItem("最近点赞", selected = content == Route.Root.RecentLike, onSelectedChange = {
                onChange(Route.Root.RecentLike)
            })*/
            NavItem(icon = Icons.Default.History,label = "最近播放", selected = content == Route.Root.RecentPlay, onSelectedChange = {
                onChange(Route.Root.RecentPlay)
            })

            NavItem(icon = Icons.AutoMirrored.Filled.QueueMusic,label = "我的歌单", selected = content is Route.Root.MyPlaylist, onSelectedChange = {
                onChange(Route.Root.MyPlaylist.Default)
            })
            /*NavItem("我的关注", selected = content == Route.Root.MySubscribe, onSelectedChange = {
               onChange(Route.Root.MySubscribe)
            })*/

            NavItem(icon = Icons.Default.Edit,label = "创作中心", selected = content is Route.Root.CreationCenter, onSelectedChange = {
                onChange(Route.Root.CreationCenter.Default)
            })

            /*NavItem("委员会中心", selected = content == Route.Root.CommitteeCenter, onSelectedChange = {
                onChange(Route.Root.CommitteeCenter)
            })*/
            NavItem(icon = Icons.Default.Groups,label = "贡献者中心", selected = content is Route.Root.ContributorCenter, onSelectedChange = {
                onChange(Route.Root.ContributorCenter.Default)
            })
        }
        Spacer(Modifier.weight(1f))
        NavItem(icon = Icons.Default.Settings,label = "设置", selected = content is Route.Root.Settings, onSelectedChange = {
            onChange(Route.Root.Settings)
        })
    }
}


@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    NavigationDrawerItem(
        modifier = modifier,
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = selected,
        onClick = {
            onSelectedChange(true)
        }
    )
    /*if (selected) {
        Button(modifier = modifier, onClick = {}) {
            Text(label)
        }
    } else {
        TextButton(modifier = modifier, onClick = { onSelectedChange(true) }) {
            Text(label)
        }
    }*/
}
