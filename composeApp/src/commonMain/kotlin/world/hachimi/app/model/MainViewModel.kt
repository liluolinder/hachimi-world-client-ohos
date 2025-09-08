package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.err
import world.hachimi.app.api.module.SongModule
import world.hachimi.app.api.ok
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
                val resp = apiClient.songModule.recentV2();
                if (resp.ok) {
                    val data = resp.ok()
                    songs = data.songs
                } else {
                    global.alert(resp.err().msg)
                }
            } catch (e: Exception) {
                Logger.e("home", "Failed to get recommend songs", e)
                global.alert("获取推荐音乐失败：${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}