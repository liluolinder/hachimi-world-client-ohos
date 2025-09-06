package world.hachimi.app.api.module

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
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

    @Serializable
    data class DetailReq(
        val reviewId: Long
    )

    @Serializable
    data class SongPublishReviewData(
        val reviewId: Long,
        val submitTime: Instant,
        val reviewTime: Instant?,
        val reviewComment: String?,
        val status: Int,
        val displayId: String,
        val title: String,
        val subtitle: String,
        val description: String,
        val durationSeconds: Int,
        val lyrics: String,
        val uploaderUid: Long,
        val uploaderName: String,
        val audioUrl: String,
        val coverUrl: String,
        val tags: List<SongModule.TagItem>,
        val productionCrew: List<SongModule.SongProductionCrew>,
        val creationType: Int,
        val originInfos: List<SongModule.CreationTypeInfo>,
    ) {
        companion object {
            const val CREATION_TYPE_ORIGINAL = 0
            const val CREATION_TYPE_DERIVATION = 1
            const val CREATION_TYPE_DERIVATION_OF_DERIVATION = 2
        }
    }


    suspend fun detail(req: DetailReq): WebResult<SongPublishReviewData> =
        client.get("/publish/review/detail", req)
}