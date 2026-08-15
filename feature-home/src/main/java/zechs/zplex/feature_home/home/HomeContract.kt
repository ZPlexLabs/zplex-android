package zechs.zplex.feature_home.home

import zechs.zplex.common.ui.mvi.UiAction
import zechs.zplex.common.ui.mvi.UiError
import zechs.zplex.common.ui.mvi.UiEvent
import zechs.zplex.common.ui.mvi.UiState
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

data class HomeState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val rows: List<HomeRow> = emptyList(),
    val error: UiError? = null
) : UiState

sealed interface HomeRow {
    val key: String

    data class ContinueWatching(val items: List<ContinueWatchingCard>) : HomeRow {
        override val key = "continue_watching"
    }

    data class Latest(
        override val key: String,
        val title: String,
        val mediaType: MediaType,
        val items: List<PosterCard>
    ) : HomeRow
}

data class ContinueWatchingCard(
    val id: Long,
    val mediaType: MediaType,
    val tmdbId: Int,
    val title: String,
    val subtitle: String,
    val backdropUrl: String?,
    val progress: Float
)

data class PosterCard(
    val tmdbId: Int,
    val mediaType: MediaType,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val rating: String?
)

sealed interface HomeAction : UiAction {
    data object Refresh : HomeAction
    data class RemoveContinueWatching(val id: Long) : HomeAction
    data class OpenDetail(val mediaType: MediaType, val tmdbId: Int) : HomeAction
}

sealed interface HomeEvent : UiEvent {
    data class NavigateToDetail(val mediaType: MediaType, val tmdbId: Int) : HomeEvent
    data class ShowMessage(val message: String) : HomeEvent
}
