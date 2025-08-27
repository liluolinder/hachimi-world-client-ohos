package world.hachimi.app.api.module

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult

class PlaylistModule(
    private val client: ApiClient
) {
    @Serializable
    data class PlaylistItem (
        val id: Long,
        val name: String,
        val coverUrl: String?,
        val description: String?,
        val createTime: Instant,
        val isPublic: Boolean,
        val songsCount: Long,
    )

    @Serializable
    data class ListResp(
        val playlists: List<PlaylistItem>,
    )

    suspend fun list(): WebResult<ListResp> =
        client.get("/playlist/list")

    @Serializable
    data class PlaylistIdReq(
        val id: Long
    )

    @Serializable
    data class DetailResp(
        val playlistInfo: PlaylistItem,
        val songs: List<SongItem>
    )

    @Serializable
    data class SongItem(
        val songDisplayId: String,
        val title: String,
        val subtitle: String,
        val coverUrl: String,
        val uploaderName: String,
        val uploaderUid: Long,
        val orderIndex: Int,
        val addTime: Instant,
    )

    suspend fun detailPrivate(req: PlaylistIdReq): WebResult<DetailResp> =
        client.get("/playlist/detail_private", req)

    @Serializable
    data class CreatePlaylistReq(
        val name: String,
        val description: String?,
        val isPublic: Boolean,
    )

    @Serializable
    data class CreatePlaylistResp(
        val id: Long,
    )

    suspend fun create(req: CreatePlaylistReq): WebResult<CreatePlaylistResp> =
        client.post("/playlist/create", req)

    @Serializable
    data class UpdatePlaylistReq(
        val id: Long,
        val name: String,
        val description: String?,
        val isPublic: Boolean,
    )

    suspend fun update(req: UpdatePlaylistReq): WebResult<Unit> =
        client.post("/playlist/update", req)

    suspend fun delete(req: PlaylistIdReq): WebResult<Unit> =
        client.post("/playlist/delete", req)

    @Serializable
    data class AddSongReq(
        val playlistId: Long,
        val songId: Long,
    )

    suspend fun addSong(req: AddSongReq): WebResult<Unit> =
        client.post("/playlist/add_song", req)

    suspend fun removeSong(req: AddSongReq): WebResult<Unit> =
        client.post("/playlist/remove_song", req)

    @Serializable
    data class ChangeOrderReq(
        val playlistId: Long,
        val songId: Long,
        /// Start from 0
        val targetOrder: Int,
    )

    suspend fun changeOrder(req: ChangeOrderReq): WebResult<Unit> =
        client.post("/playlist/change_order", req)
}