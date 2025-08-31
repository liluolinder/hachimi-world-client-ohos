package world.hachimi.app.api.module

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
}