package world.hachimi.app.ui.root

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.committee.CommitteeCenterScreen
import world.hachimi.app.ui.contributor.ContributorCenterScreen
import world.hachimi.app.ui.creation.CreationCenterScreen
import world.hachimi.app.ui.home.HomeScreen
import world.hachimi.app.ui.root.component.FooterPlayer
import world.hachimi.app.ui.root.component.SideNavigation
import world.hachimi.app.ui.root.component.TopAppBar
import world.hachimi.app.ui.search.SearchScreen
import world.hachimi.app.ui.userspace.UserSpaceScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(content: Route.Root) {
    val global = koinInject<GlobalStore>()
    Column(Modifier.fillMaxSize()) {
        TopAppBar(global)

        Row(Modifier.weight(1f).fillMaxWidth()) {
            SideNavigation(global, content)

            Spacer(Modifier.width(24.dp))

            Box(Modifier.weight(1f).fillMaxHeight()) {
                when (content) {
                    is Route.Root.Search -> SearchScreen(content.query)
                    Route.Root.Home -> HomeScreen()
                    Route.Root.RecentLike -> {}
                    Route.Root.RecentPlay -> {}
                    Route.Root.MyPlaylist -> {}
                    Route.Root.MySubscribe -> {}
                    is Route.Root.CreationCenter -> CreationCenterScreen(content)
                    Route.Root.CommitteeCenter -> CommitteeCenterScreen()
                    Route.Root.ContributorCenter -> ContributorCenterScreen()
                    Route.Root.UserSpace -> UserSpaceScreen()
                }
            }
        }

        FooterPlayer()
    }
}