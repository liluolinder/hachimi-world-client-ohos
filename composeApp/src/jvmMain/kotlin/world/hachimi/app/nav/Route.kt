package world.hachimi.app.nav

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

        data class Search(val query: String): Root()
        object UserSpace: Root()
    }
    data class Auth(val initialLogin: Boolean = true) : Route()
}