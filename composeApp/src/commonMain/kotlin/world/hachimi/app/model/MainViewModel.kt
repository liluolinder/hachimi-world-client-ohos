package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.logging.Logger

class MainViewModel(
    private val apiClient: ApiClient,
    private val global: GlobalStore
): ViewModel(CoroutineScope(Dispatchers.IO)) {
    var isLoading by mutableStateOf(false)
        private set
    var songs by mutableStateOf(emptyList<SongModule.DetailResp>())
        private set

    fun mounted() {
        getRecommendSongs()
    }

    fun unmount() {

    }

    private fun getRecommendSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true

            try {
                val recentIds = apiClient.songModule.recent().okData<SongModule.SongListResp>()
                val details = recentIds.songIds.map {
                    async { apiClient.songModule.detail(it).okData<SongModule.DetailResp>() }
                }.awaitAll()

                songs = details
            } catch (e: Exception) {
                Logger.e("home", "Failed to get recommend songs", e)
                global.alert("获取推荐音乐失败")
            } finally {
                isLoading = false
            }
        }
    }
}