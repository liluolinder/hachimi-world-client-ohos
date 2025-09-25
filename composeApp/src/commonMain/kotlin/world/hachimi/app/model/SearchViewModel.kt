package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.api.ok
import world.hachimi.app.logging.Logger

class SearchViewModel(
    private val global: GlobalStore,
    private val api: ApiClient
) : ViewModel(
    CoroutineScope(Dispatchers.Default)
) {
    enum class SearchType {
        SONG, USER, ALBUM, PLAYLIST
    }

    var searchType by mutableStateOf(SearchType.SONG)
        private set
    var query by mutableStateOf("")
        private set
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var loading by mutableStateOf(false)
    val songData = mutableStateListOf<SongModule.SearchSongItem>()
    val userData = mutableStateListOf<UserModule.PublicUserProfile>()
    var searchProcessingTimeMs by mutableStateOf(0L)
        private set

    fun mounted(query: String, searchType: SearchType) {
        this.query = query
        search()
    }

    fun dispose() {

    }


    fun search() = viewModelScope.launch {
        when (searchType) {
            SearchType.SONG -> searchSongs()
            SearchType.USER -> searchUsers()
            SearchType.ALBUM -> {}
            SearchType.PLAYLIST -> {}
        }
    }

    fun updateSearchType(type: SearchType) = viewModelScope.launch {
        searchType = type
        search()
    }

    private suspend fun searchSongs() {
        loading = true
        try {
            songData.clear()
            val resp = api.songModule.search(
                SongModule.SearchReq(
                    q = query,
                    limit = null,
                    offset = null,
                    filter = null
                )
            )
            if (resp.ok) {
                val data = resp.okData<SongModule.SearchResp>()
                Snapshot.withMutableSnapshot {
                    songData.clear()
                    songData.addAll(data.hits)
                    searchProcessingTimeMs = data.processingTimeMs
                }
            } else {
                val err = resp.err()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e("search", "Failed to search", e)
            global.alert(e.message)
        } finally {
            loading = false
        }
    }

    private suspend fun searchUsers() {
        loading = true
        try {
            userData.clear()
            val resp = api.userModule.search(
                UserModule.SearchReq(
                    q = query,
                    page = 0,
                    size = 20
                )
            )
            if (resp.ok) {
                val data = resp.ok()
                Snapshot.withMutableSnapshot {
                    userData.clear()
                    userData.addAll(data.hits)
                    searchProcessingTimeMs = data.processingTimeMs
                }
            } else {
                val err = resp.err()
                global.alert(err.msg)
            }
        } catch (e: Throwable) {
            Logger.e("search", "Failed to search", e)
            global.alert(e.message)
        } finally {
            loading = false
        }
    }
}