package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.util.logging.error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.module.PlaylistModule
import world.hachimi.app.logging.Logger
import kotlin.time.Duration.Companion.seconds

class PlaylistDetailViewModel(
    private val api: ApiClient,
    private val global: GlobalStore
) : ViewModel(CoroutineScope(Dispatchers.IO)) {
    var loading by mutableStateOf(false)
        private set
    var playlistId by mutableStateOf<Long?>(null)
    var playlistInfo by mutableStateOf<PlaylistModule.PlaylistItem?>(null)
    var songs by mutableStateOf<List<PlaylistModule.SongItem>>(emptyList())

    var showEditDialog by mutableStateOf(false)
    var editName by mutableStateOf("")
    var editDescription by mutableStateOf("")
    var editOperating by mutableStateOf(false)
    var editPrivate by mutableStateOf(false)

    fun mounted(playlistId: Long) {
        this.playlistId = playlistId

        viewModelScope.launch {
            refresh()
        }
    }

    fun dispose() {

    }

    fun edit() {
        playlistInfo?.let {
            editName = it.name
            editDescription = it.description ?: ""
            editPrivate = !it.isPublic
            showEditDialog = true
        }
    }

    fun confirmEdit() {
        viewModelScope.launch {
            playlistId?.let {id->
                editOperating = false
                try {
                    val resp = api.playlistModule.update(PlaylistModule.UpdatePlaylistReq(
                        id = id,
                        name = editName,
                        description = editDescription.takeIf { it.isNotBlank() },
                        isPublic = !editPrivate
                    ))
                    if (resp.ok) {
                        showEditDialog = false
                        refresh()
                    } else {
                        val data = resp.errData<CommonError>()
                        global.alert(data.msg)
                    }
                } catch (e: Exception) {
                    Logger.e("playlist", "Failed to edit playlist", e)
                    global.alert(e.message)
                } finally {
                    editOperating = false
                }
            }
        }
    }

    fun cancelEdit() {
        showEditDialog = false
    }

    fun playAll() {
        viewModelScope.launch {
            val items = songs.map { GlobalStore.MusicQueueItem(
                id = it.songId,
                displayId = it.songDisplayId,
                name = it.title,
                artist = it.uploaderName,
                duration = it.durationSeconds.seconds,
                coverUrl = it.coverUrl
            ) }
            global.playAll(items)
        }
    }

    private suspend fun refresh() {
        loading = true
        try {
            val resp = api.playlistModule.detailPrivate(PlaylistModule.PlaylistIdReq(playlistId!!))
            if (resp.ok) {
                val data = resp.okData<PlaylistModule.DetailResp>()
                playlistInfo = data.playlistInfo
                songs = data.songs
            } else {
                global.alert(resp.errData<CommonError>().msg)
            }
        } catch (e: Exception) {
            Logger.e("playlist detail", "Failed to refresh playlist detail", e)
            global.alert(e.message)
        } finally {
            loading = false
        }
    }
}