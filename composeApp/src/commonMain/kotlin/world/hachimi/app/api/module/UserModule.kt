package world.hachimi.app.api.module

import io.ktor.client.content.ProgressListener
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.timeout
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
    data class PublicUserProfile(
        val uid: Long,
        val username: String,
        val avatarUrl: String?,
        val bio: String?,
        val gender: Int?,
        val isBanned: Boolean,
    )

    suspend fun profile(uid: Long): WebResult<PublicUserProfile> =
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
            timeout {
                connectTimeoutMillis = 30_000
                requestTimeoutMillis = 30_000
            }
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

    @Serializable
    data class SearchReq(
        val q: String,
        val page: Int,
        val size: Int,
    )

    @Serializable
    data class SearchResp(
        val hits: List<PublicUserProfile>,
        val query: String,
        val processingTimeMs: Long,
        val totalHits: Long?,
        val limit: Long,
        val offset: Long,
    )

    suspend fun search(req: SearchReq): WebResult<SearchResp> =
        client.get("/user/search", req, false)
}
