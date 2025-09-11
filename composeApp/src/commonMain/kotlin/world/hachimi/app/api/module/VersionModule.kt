package world.hachimi.app.api.module

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult

class VersionModule(
    private val client: ApiClient
) {
    @Serializable
    data class ServerVersion(
        val version: Int,
        val minVersion: Int
    )

    suspend fun server(): WebResult<ServerVersion> =
        client.get("/version/server")

    @Serializable
    data class LatestVersionReq (
        val variant : String
    )

    @Serializable
    data class LatestVersionResp (
        val versionName: String,
        val versionNumber: Int,
        val changelog: String,
        val variant: String,
        val url: String,
        val releaseTime : Instant
    )

    suspend fun latest(req: LatestVersionReq): WebResult<LatestVersionResp?> =
        client.get("/version/latest", req)
}