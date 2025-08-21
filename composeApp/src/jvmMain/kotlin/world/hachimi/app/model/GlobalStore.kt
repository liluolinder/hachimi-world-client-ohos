package world.hachimi.app.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList

object GlobalStore {
    val nav = Navigator("home")
}

class Navigator(start: String) {
    val backStack: SnapshotStateList<String> = mutableStateListOf(start)

    fun replace(vararg routes: String) {
        Snapshot.withMutableSnapshot {
            backStack.clear()
            backStack.addAll(routes)
        }
    }

    fun navigateTo(route: String) {
        Snapshot.withMutableSnapshot {
            backStack.removeLastOrNull()
            backStack.add(route)
        }
    }

    fun push(route: String) {
        backStack.add(route)
    }

    fun back() {
        backStack.removeLastOrNull()
    }
}