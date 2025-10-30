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
            // If the query text is display id: JM-ABCD-123, use detail to get information.
            // The `JM-` prefix and dash `-` is optional
            val displayIdPattern = "^(?:JM-)?([A-Z]{3,4})-?(\\d{3})$".toRegex()
            val matchResult = displayIdPattern.find(query.trim().uppercase())
            if (matchResult != null) {
                val part1 = matchResult.groupValues[1]
                val part2 = matchResult.groupValues[2]
                val displayId = "JM-$part1-$part2"
                val resp = api.songModule.detail(displayId)
                if (resp.ok) {
                    val data = resp.okData<SongModule.PublicSongDetail>()
                    Snapshot.withMutableSnapshot {
                        songData.add(SongModule.SearchSongItem(
                            id = data.id,
                            displayId = data.displayId,
                            title = data.title,
                            subtitle = data.subtitle,
                            description = data.description,
                            artist = data.uploaderName,
                            durationSeconds = data.durationSeconds,
                            playCount = data.playCount,
                            likeCount = data.likeCount,
                            coverArtUrl = data.coverUrl,
                            audioUrl = data.audioUrl,
                            uploaderUid = data.uploaderUid,
                            uploaderName = data.uploaderName,
                        ))
                        searchProcessingTimeMs = 0
                    }
                }
            }

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
            if (query.toLongOrNull() != null) {
                val resp = api.userModule.profile(query.toLong())
                if (resp.ok) {
                    val data = resp.ok()
                    Snapshot.withMutableSnapshot {
                        userData.add(data)
                        searchProcessingTimeMs = 0
                    }
                } else {
                    val err = resp.err()
                    global.alert(err.msg)
                }
            }

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