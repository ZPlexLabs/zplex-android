package zechs.zplex.feature_downloads.downloads

import zechs.zplex.common.player.PlayerArgs
import zechs.zplex.common.ui.mvi.UiAction
import zechs.zplex.common.ui.mvi.UiEvent
import zechs.zplex.common.ui.mvi.UiState
import zechs.zplex.zplex_api.data.local.downloads.DownloadEntity

data class DownloadsState(
    val isLoading: Boolean = true,
    val items: List<DownloadEntity> = emptyList(),
    val storageUsedBytes: Long = 0L
) : UiState

sealed interface DownloadsAction : UiAction {
    data class Pause(val id: String) : DownloadsAction
    data class Resume(val id: String) : DownloadsAction
    data class Cancel(val id: String) : DownloadsAction
    data class Delete(val id: String) : DownloadsAction
    data class Play(val item: DownloadEntity) : DownloadsAction
}

sealed interface DownloadsEvent : UiEvent {
    data class NavigateToPlayer(val args: PlayerArgs) : DownloadsEvent
}
