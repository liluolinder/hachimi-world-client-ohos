package world.hachimi.app.nav

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import world.hachimi.app.nav.Route.Root

class Navigator(start: Any) {
    val backStack: SnapshotStateList<Any> = mutableStateListOf(start)

    fun replace(vararg routes: Any) {
        Snapshot.withMutableSnapshot {
            backStack.clear()
            backStack.addAll(routes)
        }
    }

    fun navigateTo(route: Any) {
        Snapshot.withMutableSnapshot {
            backStack.removeLastOrNull()
            backStack.add(route)
        }
    }

    fun push(route: Any) {
        backStack.add(route)
    }

    fun back() {
        Snapshot.withMutableSnapshot {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        }
    }
}

sealed class Route {
    data class Root(val child: RootContent) : Route()
    data class Auth(val initialLogin: Boolean = true) : Route()
    data object Home: Route()
}

sealed class RootContent {
    data object Home: RootContent()
    data object RecentPlay: RootContent()
    data object RecentLike: RootContent()
    data object MySubscribe: RootContent()
    data object MyPlaylist: RootContent()
    data object CreationCenter: RootContent()
    data object CommitteeCenter: RootContent()
    data object ContributorCenter: RootContent()
}