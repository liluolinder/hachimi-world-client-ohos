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
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.api.ok
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger

class ReviewDetailViewModel(
    private val api: ApiClient,
    private val global: GlobalStore,
): ViewModel(CoroutineScope(Dispatchers.IO)) {
    var initializeStatus by mutableStateOf(InitializeStatus.INIT)
        private set
    var reviewId by mutableStateOf(0L)
        private set
    var loading by mutableStateOf(false)
        private set
    var data by mutableStateOf<PublishModule.SongPublishReviewData?>(null)
        private set
    fun mounted(reviewId: Long) {
        this.reviewId = reviewId
        refresh()
    }

    fun dispose() {

    }

    fun refresh() = viewModelScope.launch {
        loading = true
        try {
            val resp = api.publishModule.reviewDetail(PublishModule.DetailReq(reviewId))
            if (resp.ok) {
                data = resp.ok()
                initializeStatus = InitializeStatus.LOADED
            } else {
                global.alert(resp.err().msg)
                initializeStatus = InitializeStatus.FAILED
            }
        } catch (e: Exception) {
            Logger.e("review_detail", "Failed to fetch review detail", e)
            global.alert(e.message)
            initializeStatus = InitializeStatus.FAILED
        } finally {
            loading = false
        }
    }

    fun download() {
        data?.let {
            getPlatform().openUrl(it.audioUrl)
        }
    }
}