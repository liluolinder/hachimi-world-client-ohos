package world.hachimi.app.api.module

import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlinx.serialization.Serializable


class UserModule(
    private val client: ApiClient
) {
    @Serializable
    data class ProfileResponse(
        val uid: Long,
        val username: String,
        val email: String,
    )

    suspend fun profile(): WebResult<ProfileResponse> =
        client.get("/user/profile")


    @Serializable
    data class UpdateProfileReq (
        val username: String,
        val bio: String?,
        val gender: Int?,
    )

    suspend fun updateProfile(req: UpdateProfileReq): WebResult<Unit> =
        client.post("/user/update_profile", req)
}