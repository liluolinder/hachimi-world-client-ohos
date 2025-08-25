package world.hachimi.app.nav

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList

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
    sealed class Root : Route() {
        companion object {
            val Default = Home
        }
        data object Home: Root()
        data object RecentPlay: Root()
        data object RecentLike: Root()
        data object MySubscribe: Root()
        data object MyPlaylist: Root()
        sealed class CreationCenter: Root() {
            companion object Companion {
                val Default = MyArtwork
            }

            object MyArtwork: CreationCenter()
            object Publish: CreationCenter()
        }
        data object CommitteeCenter: Root()
        data object ContributorCenter: Root()
    }
    data class Auth(val initialLogin: Boolean = true) : Route()
}