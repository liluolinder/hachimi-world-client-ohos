package world.hachimi.app.api.module

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult

class PublishModule(
    private val client: ApiClient
) {
    @Serializable
    data class PageReq(
        val pageIndex: Long,
        val pageSize: Long,
    )

    @Serializable
    data class PageResp(
        val data: List<SongPublishReviewBrief>,
        val pageIndex: Long,
        val pageSize: Long,
        val total: Long,
    )

    @Serializable
    data class SongPublishReviewBrief (
       val reviewId: Long,
       val title: String,
       val subtitle: String,
       val artist: String,
       val coverUrl: String,
       val submitTime: Instant,
       val reviewTime: Instant?,
       val status: Int,
    ) {
        companion object {
            const val STATUS_PENDING = 0
            const val STATUS_APPROVED = 1
            const val STATUS_REJECTED = 2
        }
    }


    suspend fun page(req: PageReq): WebResult<PageResp> =
        client.get("/publish/review/page", req)

    suspend fun pageContributor(req: PageReq): WebResult<PageResp> =
        client.get("/publish/review/page_contributor", req)
}