package world.hachimi.app.ui.root.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Route

@Composable
fun SideNavigation(
    global: GlobalStore,
    content: Route
) {
    Column(Modifier.defaultMinSize(minWidth = 300.dp)) {
        NavItem("首页", selected = content == Route.Root.Home, onSelectedChange = {
            global.nav.push(Route.Root.Home)
        })
        if (global.isLoggedIn) {
            /*NavItem("最近点赞", selected = content == Route.Root.RecentLike, onSelectedChange = {
                global.nav.push(Route.Root.RecentLike)
            })*/
            NavItem("最近播放", selected = content == Route.Root.RecentPlay, onSelectedChange = {
                global.nav.push(Route.Root.RecentPlay)
            })

            NavItem("我的歌单", selected = content is Route.Root.MyPlaylist, onSelectedChange = {
                global.nav.push(Route.Root.MyPlaylist.Default)
            })
            /*NavItem("我的关注", selected = content == Route.Root.MySubscribe, onSelectedChange = {
                global.nav.push(Route.Root.MySubscribe)
            })*/

            NavItem("创作中心", selected = content is Route.Root.CreationCenter, onSelectedChange = {
                global.nav.push(Route.Root.CreationCenter.Default)
            })

            /*NavItem("委员会中心", selected = content == Route.Root.CommitteeCenter, onSelectedChange = {
                global.nav.push(Route.Root.CommitteeCenter)
            })*/
            NavItem("维护者中心", selected = content is Route.Root.ContributorCenter, onSelectedChange = {
                global.nav.push(Route.Root.ContributorCenter.Default)
            })
        }
        Spacer(Modifier.weight(1f))
        NavItem("设置", selected = content is Route.Root.Settings, onSelectedChange = {
            global.nav.push(Route.Root.Settings)
        })
    }
}


@Composable
private fun NavItem(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    NavigationDrawerItem(
        modifier = modifier,
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
