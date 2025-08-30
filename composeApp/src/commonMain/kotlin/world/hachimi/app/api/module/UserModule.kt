package world.hachimi.app.api.module

import io.ktor.client.content.ProgressListener
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.io.Source
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult
import kotlinx.serialization.Serializable


class UserModule(
    private val client: ApiClient
) {

    @Serializable
    data class ProfileReq(
        val uid: Long
    )

    @Serializable
    data class ProfileResp(
        val uid: Long,
        val username: String,
        val avatarUrl: String?,
        val bio: String?,
        val gender: Int?,
        val isBanned: Boolean,
    )

    suspend fun profile(uid: Long): WebResult<ProfileResp> =
        client.get("/user/profile", ProfileReq(uid))

    @Serializable
    data class UpdateProfileReq(
        val username: String,
        val bio: String?,
        val gender: Int?,
    )

    suspend fun updateProfile(req: UpdateProfileReq): WebResult<Unit> =
        client.post("/user/update_profile", req)

    suspend fun setAvatar(filename: String, source: Source, listener: ProgressListener? = null): WebResult<Unit> {
        return client.postWith("/user/set_avatar") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "image",
                            InputProvider { source },
                            headersOf(HttpHeaders.ContentDisposition, "filename=\"${filename}\"")
                        )
                    }
                )
            )
            onUpload(listener)
        }
    }
}
