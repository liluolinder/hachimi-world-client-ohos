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
        sealed class MyPlaylist: Root() {
            companion object {
                val Default = List
            }
            data object List: MyPlaylist()
            data class Detail(val playlistId: Long): MyPlaylist()
        }
        sealed class CreationCenter: Root() {
            companion object Companion {
                val Default = MyArtwork
            }

            object MyArtwork: CreationCenter()
            object Publish: CreationCenter()
        }
        data object CommitteeCenter: Root()
        sealed class ContributorCenter: Root() {
            companion object {
                val Default = ReviewList
            }
            data object ReviewList: ContributorCenter()
            data class ReviewDetail(val reviewId: Long): ContributorCenter()
        }

        data class Search(val query: String): Root()
        object UserSpace: Root()
        data object Settings: Root()
    }
    data class Auth(val initialLogin: Boolean = true) : Route()
}