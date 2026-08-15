package zechs.zplex.feature_search.search

import zechs.zplex.common.ui.mvi.UiAction
import zechs.zplex.common.ui.mvi.UiError
import zechs.zplex.common.ui.mvi.UiEvent
import zechs.zplex.common.ui.mvi.UiState
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

data class SearchState(
    val query: String = "",
    val isLoading: Boolean = true,
    val suggestions: List<SearchResultItem> = emptyList(),
    val results: List<SearchResultItem> = emptyList(),
    val error: UiError? = null
) : UiState

data class SearchResultItem(
    val tmdbId: Int,
    val mediaType: MediaType,
    val title: String,
    val typeLabel: String
)

sealed interface SearchAction : UiAction {
    data class QueryChanged(val query: String) : SearchAction
    data object ClearQuery : SearchAction
    data object Retry : SearchAction
    data class OpenDetail(val mediaType: MediaType, val tmdbId: Int) : SearchAction
}

sealed interface SearchEvent : UiEvent {
    data class NavigateToDetail(val mediaType: MediaType, val tmdbId: Int) : SearchEvent
}
