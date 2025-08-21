package world.hachimi.app.api.module

import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult

class SongModule(
    private val client: ApiClient
) {
    @Serializable
    data class SongListResp(
        val songIds: List<String>
    )

    suspend fun recent(): WebResult<SongListResp> = client.get("/song/recent", false)

    suspend fun hot(): WebResult<SongListResp> = client.get("/song/hot", false)

    @Serializable
    data class DetailResp(
        val id: String,
        val title: String,
        val subtitle: String,
        val description: String,
        val tags: List<TagItem>,
        val lyrics: String,
        val audioUrl: String,
        val coverUrl: String,
        val productionCrew: List<SongProductionCrew>,
        val creationType: Int,
        val originInfos: List<CreationTypeInfo>,
        val uploaderUid: Long,
        val playCount: Long,
        val likeCount: Long,
    )

    @Serializable
    data class TagItem(
        val id: Long,
        val name: String,
        val description: String?,
    )

    @Serializable
    data class SongProductionCrew(
        val id: Long,
        val role: String,
        val uid: Long?,
        val personName: String?,
    )

    @Serializable
    data class CreationTypeInfo(
        // If `song_id` is Some, the rest fields could be None
        val songDisplayId: String?,
        val title: String?,
        val artist: String?,
        val url: String?,
        val originType: Long,
    )

    @Serializable
    data class DetailReq(
        /// Actually displayed id
        val id: String,
    )

    suspend fun detail(songId: String): WebResult<DetailResp> =
        client.get("/song/detail", DetailReq(songId), false)
}