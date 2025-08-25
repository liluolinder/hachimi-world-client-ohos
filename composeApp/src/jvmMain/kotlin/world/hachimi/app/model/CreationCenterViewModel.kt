package world.hachimi.app.model

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.logging.Logger

class CreationCenterViewModel(
    private val api: ApiClient
) : ViewModel(CoroutineScope(Dispatchers.IO)) {
    var title by mutableStateOf("")
    var subtitle by mutableStateOf("")
    val tags = mutableStateListOf<String>()
    var description by mutableStateOf("")
    var lyrics by mutableStateOf("")

    var creationType by mutableStateOf(0)
    var originId by mutableStateOf("")
    var originTitle by mutableStateOf("")
    var originLink by mutableStateOf("")
    var deriveId by mutableStateOf("")
    var deriveTitle by mutableStateOf("")
    var deriveLink by mutableStateOf("")

    var coverImageUploadProgress by mutableStateOf(0f)
        private set
    var coverImageUploading by mutableStateOf(false)
        private set
    var coverImage by mutableStateOf<PlatformFile?>(null)
        private set
    private var coverTempId: String? = null
    var error by mutableStateOf<String?>(null)
        private set

    var audioUploadProgress by mutableStateOf(0f)
        private set
    var audioUploading by mutableStateOf(false)
        private set
    var audioFileName by mutableStateOf("")
    var audioDurationSecs by mutableStateOf(0)
        private set
    var audioUploaded by mutableStateOf(false)
    private var audioTempId: String? = null

    var isOperating by mutableStateOf(false)

    val crews = mutableStateListOf<CrewItem>()

    data class CrewItem(
        val uid: String,
        val name: String,
        val role: String
    )

    val externalLinks = mutableStateListOf<SongModule.PublishReq.ExternalLink>()
    var publishedSongId by mutableStateOf<String?>(null)
        private set
    var showSuccessDialog by mutableStateOf(false)
        private set

    fun setAudioFile() {
        viewModelScope.launch(Dispatchers.Default) {
            val audio = FileKit.openFilePicker(
                type = FileKitType.File("mp3", "flac")
            )
            if (audio != null) {
                clearError()

                // 1. Validate
                val size = audio.size()
                if (size > 20 * 1024 * 1024) {
                    error = "Audio too large"
                    return@launch
                }
                val buffer = audio.source().buffered()

                // 2. Upload
                val data = try {
                    audioUploading = true
                    audioUploadProgress = 0f

                    val resp = api.songModule.uploadAudioFile(
                        filename = audio.name,
                        source = buffer,
                        listener = { sent, total ->
                            audioUploadProgress = (sent.toDouble() / size).toFloat()
                        }
                    )
                    if (!resp.ok) {
                        error = resp.errData<CommonError>().msg
                        return@launch
                    }

                    resp.okData<SongModule.UploadAudioFileResp>()
                } catch (e: Exception) {
                    Logger.e("creation", "Failed to upload audio file", e)
                    error = e.localizedMessage
                    return@launch
                } finally {
                    audioUploading = false
                }

                data.title?.let {
                    title = it
                }

                // 3. Save temp id
                Snapshot.withMutableSnapshot {
                    audioTempId = data.tempId
                    audioFileName = audio.name
                    audioDurationSecs = data.durationSecs.toInt()
                    audioUploaded = true
                }
            }
        }
    }

    fun setCoverImage() {
        viewModelScope.launch(Dispatchers.Default) {
            val image = FileKit.openFilePicker(
                type = FileKitType.Image
            )
            if (image != null) {
                clearError()

                // 1. Validate image
                val size = image.size()
                if (size > 10 * 1024 * 1024) {
                    error = "Image too large"
                    return@launch
                }
                val buffer = image.source().buffered()
                coverImage = image

                // 2. Upload
                val data = try {
                    coverImageUploading = true
                    coverImageUploadProgress = 0f

                    val resp = api.songModule.uploadCoverImage(
                        filename = image.name,
                        source = buffer,
                        listener = { sent, total ->
                            coverImageUploadProgress = (sent.toDouble() / size).toFloat()
                        }
                    )
                    if (!resp.ok) {
                        error = resp.errData<CommonError>().msg
                        return@launch
                    }

                    resp.okData<SongModule.UploadImageResp>()
                } catch (e: Exception) {
                    Logger.e("creation", "Failed to upload cover image", e)
                    error = e.localizedMessage
                    return@launch
                } finally {
                    coverImageUploading = false
                }

                // 3. Save temp id
                coverTempId = data.tempId
            }
        }
    }

    fun addCrewMember(
        uid: String,
        name: String,
        role: String
    ) {
        crews.add(CrewItem(uid, name, role))
    }

    fun removeCrewMember(index: Int) {
        crews.removeAt(index)
    }

    fun clearError() {
        error = null
    }

    fun addTag(name: String) {
        // TODO: Send add tag request
        tags.add(name)
    }

    fun removeTag(index: Int) {
        tags.removeAt(index)
    }

    fun addLink(platform: String, link: String) {
        externalLinks.add(SongModule.PublishReq.ExternalLink(platform, link))
    }

    fun removeLink(index: Int) {
        externalLinks.removeAt(index)
    }

    val publishEnabled by derivedStateOf {
        checkPublishEnabled()
    }

    private fun checkPublishEnabled(): Boolean {
        val basic = audioTempId != null
                && coverTempId != null
                && title.isNotBlank()
                && subtitle.isNotBlank()
                && description.isNotBlank()
                && lyrics.isNotBlank()

        val originCheck = if (creationType > 0) {
            originId.isNotBlank() || (originTitle.isNotBlank() /*&& originLink.isNotBlank()*/)
        } else true
        val derivationCheck = if (creationType > 1) {
            deriveId.isNotBlank() || (deriveTitle.isNotBlank() /*&& deriveLink.isNotBlank()*/)
        } else true

        return basic && originCheck && derivationCheck && !isOperating
    }

    fun publish() {
        viewModelScope.launch(Dispatchers.IO) {
            if (checkPublishEnabled()) {
                try {
                    isOperating = true

                    val creationInfo = SongModule.PublishReq.CreationInfo(
                        creationType = creationType,
                        originInfo = if (creationType > 0) SongModule.CreationTypeInfo(
                            songDisplayId = originId.takeIf { it.isNotBlank() },
                            title = originTitle.takeIf { it.isNotBlank() },
                            url = originLink.takeIf { it.isNotBlank() },
                            artist = null,
                            originType = 0
                        ) else null,
                        derivativeInfo = if (creationType > 1) SongModule.CreationTypeInfo(
                            songDisplayId = deriveId.takeIf {  it.isNotBlank() },
                            title = deriveTitle.takeIf {  it.isNotBlank() },
                            url = deriveLink.takeIf {  it.isNotBlank() },
                            artist = null,
                            originType = 1
                        ) else null
                    )


                    val crew = crews.map {
                        SongModule.PublishReq.ProductionItem(
                            role = it.role,
                            uid = it.uid.toLong(),
                            name = it.name
                        )
                    }

                    val resp = api.songModule.publish(
                        SongModule.PublishReq(
                            songTempId = audioTempId!!,
                            coverTempId = coverTempId!!,
                            title = title,
                            subtitle = subtitle,
                            description = description,
                            lyrics = lyrics,
                            tagIds = emptyList(), // TODO: Do this later
                            creationInfo = creationInfo,
                            productionCrew = crew,
                            externalLinks = externalLinks
                        )
                    )
                    if (resp.ok) {
                        val data = resp.okData<SongModule.PublishResp>()
                        publishedSongId = data.songDisplayId
                        showSuccessDialog = true
                    } else {
                        error = resp.errData<CommonError>().msg
                    }
                } catch (e: Exception) {
                    Logger.e("creation", "Failed to publish song", e)
                    error = e.localizedMessage
                } finally {
                    isOperating = false
                }
            }
        }
    }

    fun closeDialog() {
        showSuccessDialog = false
    }
}