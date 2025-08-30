package world.hachimi.app.ui.playlist

import androidx.compose.runtime.Composable
import world.hachimi.app.nav.Route

@Composable
fun PlaylistRouteScreen(child: Route.Root.MyPlaylist) {
    when (child) {
        is Route.Root.MyPlaylist.Detail -> PlaylistDetailScreen(child.playlistId)
        Route.Root.MyPlaylist.List -> PlaylistScreen()
    }
}