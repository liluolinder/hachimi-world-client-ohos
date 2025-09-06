package world.hachimi.app.ui.contributor

import androidx.compose.runtime.Composable
import world.hachimi.app.nav.Route

@Composable
fun ContributorCenterScreen(child: Route.Root.ContributorCenter) {
    when (child) {
        Route.Root.ContributorCenter.ReviewList -> ReviewScreen()
        is Route.Root.ContributorCenter.ReviewDetail -> ReviewDetailScreen(child.reviewId)
    }
}