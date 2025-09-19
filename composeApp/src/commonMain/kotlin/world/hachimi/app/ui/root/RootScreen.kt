package world.hachimi.app.ui.root

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.committee.CommitteeCenterScreen
import world.hachimi.app.ui.component.Logo
import world.hachimi.app.ui.contributor.ContributorCenterScreen
import world.hachimi.app.ui.creation.CreationCenterScreen
import world.hachimi.app.ui.home.HomeScreen
import world.hachimi.app.ui.player.FooterPlayer
import world.hachimi.app.ui.playlist.PlaylistRouteScreen
import world.hachimi.app.ui.recentplay.RecentPlayScreen
import world.hachimi.app.ui.root.component.SideNavigation
import world.hachimi.app.ui.root.component.TopAppBar
import world.hachimi.app.ui.search.SearchScreen
import world.hachimi.app.ui.settings.SettingsScreen
import world.hachimi.app.ui.userspace.UserSpaceScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(routeContent: Route.Root) {
    val global = koinInject<GlobalStore>()
    AdaptiveScreen(
        navigationContent = { onChange ->
            SideNavigation(content = routeContent, onChange = {
                global.nav.push(it)
                onChange(it)
            })
        },
        content = {
            AnimatedContent(routeContent) { routeContent ->
                when (routeContent) {
                    is Route.Root.Search -> SearchScreen(routeContent.query)
                    Route.Root.Home -> HomeScreen()
                    Route.Root.RecentLike -> {}
                    Route.Root.RecentPlay -> RecentPlayScreen()
                    is Route.Root.MyPlaylist -> PlaylistRouteScreen(routeContent)
                    Route.Root.MySubscribe -> {}
                    is Route.Root.CreationCenter -> CreationCenterScreen(routeContent)
                    Route.Root.CommitteeCenter -> CommitteeCenterScreen()
                    is Route.Root.ContributorCenter -> ContributorCenterScreen(routeContent)
                    Route.Root.UserSpace -> UserSpaceScreen()
                    Route.Root.Settings -> SettingsScreen()
                }
            }
        }
    )

}

@Composable
private fun AdaptiveScreen(
    navigationContent: @Composable (onChange: (Route) -> Unit) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints {
        if (maxWidth < 600.dp) { // Compact
            CompactScreen({state ->
                navigationContent {
                    scope.launch {
                        state.close()
                    }
                }
            }, content)
        } else {
            ExpandedScreen({
                navigationContent({})
            }, content)
        }
    }
}

@Composable
private fun CompactScreen(
    navigationContent: @Composable (drawerState: DrawerState) -> Unit,
    content: @Composable () -> Unit,
    global: GlobalStore = koinInject()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(Modifier.width(300.dp)) {
                Logo(Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp))
                Box(Modifier.padding(12.dp)) {
                    navigationContent(drawerState)
                }
            }
        }
    ) {
        Scaffold(
            topBar = { TopAppBar(global, onExpandNavClick = {
                scope.launch {
                    drawerState.open()
                }
            }) },
            bottomBar = { FooterPlayer() }
        ) {
            Box(Modifier.padding(it)) {
                content()
            }
        }
    }
}

@Composable
private fun ExpandedScreen(
    navigationContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
    global: GlobalStore = koinInject()
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(global, {})

        Row(Modifier.weight(1f).fillMaxWidth()) {
            Box(Modifier.padding(start = 24.dp, top = 24.dp, bottom = 24.dp).width(300.dp)) {
                navigationContent()
            }
            Spacer(Modifier.width(24.dp))
            Box(Modifier.weight(1f).fillMaxHeight()) { content() }
        }

        FooterPlayer()
    }
}