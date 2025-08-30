package world.hachimi.app.ui.creation

import androidx.compose.runtime.Composable
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.creation.artwork.MyArtworkScreen
import world.hachimi.app.ui.creation.publish.PublishScreen

@Composable
fun CreationCenterScreen(
    child: Route.Root.CreationCenter
) {
    when (child) {
        Route.Root.CreationCenter.MyArtwork -> MyArtworkScreen()
        Route.Root.CreationCenter.Publish -> PublishScreen()
    }
}