package zechs.zplex.feature_downloads.downloads

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import zechs.zplex.common.player.PlayerArgs
import zechs.zplex.common.player.PlayerItem
import zechs.zplex.common.ui.mvi.MviViewModel
import zechs.zplex.zplex_api.data.local.downloads.DownloadEntity
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.repository.DownloadRepository
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : MviViewModel<DownloadsState, DownloadsAction, DownloadsEvent>(DownloadsState()) {

    init {
        downloadRepository.observeDownloads()
            .onEach { list ->
                setState {
                    copy(
                        isLoading = false,
                        items = list,
                        storageUsedBytes = list.sumOf { it.downloadedBytes }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: DownloadsAction) {
        when (action) {
            is DownloadsAction.Pause -> viewModelScope.launch { downloadRepository.pause(action.id) }
            is DownloadsAction.Resume -> viewModelScope.launch { downloadRepository.resume(action.id) }
            is DownloadsAction.Cancel -> viewModelScope.launch { downloadRepository.cancel(action.id) }
            is DownloadsAction.Delete -> viewModelScope.launch { downloadRepository.delete(action.id) }
            is DownloadsAction.Play -> play(action.item)
        }
    }

    private fun play(item: DownloadEntity) {
        val playerItem = PlayerItem(
            fileId = item.fileId,
            tmdbId = item.tmdbId,
            isTv = item.mediaType == MediaType.SHOW,
            title = item.title,
            subtitle = item.subtitle,
            seasonNumber = item.seasonNumber ?: 0,
            episodeNumber = item.episodeNumber ?: 0
        )
        sendEvent(DownloadsEvent.NavigateToPlayer(PlayerArgs(listOf(playerItem), 0, 0L)))
    }
}
