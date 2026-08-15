package zechs.zplex.feature_settings.history

import zechs.zplex.common.ui.mvi.UiAction
import zechs.zplex.common.ui.mvi.UiError
import zechs.zplex.common.ui.mvi.UiEvent
import zechs.zplex.common.ui.mvi.UiState
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

data class HistoryState(
    val isLoading: Boolean = true,
    val items: List<HistoryRow> = emptyList(),
    val error: UiError? = null
) : UiState

data class HistoryRow(
    val id: Long,
    val mediaType: MediaType,
    val tmdbId: Int,
    val title: String,
    val subtitle: String,
    val posterUrl: String?,
    val progress: Float
)

sealed interface HistoryAction : UiAction {
    data object Retry : HistoryAction
    data class Remove(val id: Long) : HistoryAction
}

sealed interface HistoryEvent : UiEvent
