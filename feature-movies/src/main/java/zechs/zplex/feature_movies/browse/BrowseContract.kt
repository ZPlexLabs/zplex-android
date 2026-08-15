package zechs.zplex.feature_movies.browse

import zechs.zplex.common.ui.mvi.UiAction
import zechs.zplex.common.ui.mvi.UiEvent
import zechs.zplex.common.ui.mvi.UiState
import zechs.zplex.zplex_api.data.browse.FilterQuery
import zechs.zplex.zplex_api.data.remote.api.MediaListItem
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.enums.OrderBy
import zechs.zplex.zplex_api.data.remote.api.enums.SortBy

data class BrowseState(
    val mediaType: MediaType,
    val sortBy: SortBy = SortBy.DATE_ADDED,
    val orderBy: OrderBy = OrderBy.DESC,
    val filter: FilterQuery = FilterQuery(),
    val sections: List<FilterSection> = emptyList(),
    val selectedItem: MediaListItem? = null
) : UiState {
    val activeFilterCount: Int get() = filter.selectionCount
}

sealed interface BrowseAction : UiAction {
    data class SetSort(val sortBy: SortBy, val orderBy: OrderBy) : BrowseAction
    data class ApplyFilter(val filter: FilterQuery) : BrowseAction
    data object ClearFilter : BrowseAction
    data class SelectItem(val item: MediaListItem) : BrowseAction
    data class OpenDetail(val item: MediaListItem) : BrowseAction
}

sealed interface BrowseEvent : UiEvent {
    data class NavigateToDetail(val mediaType: MediaType, val tmdbId: Int) : BrowseEvent
}

enum class SectionKey { GENRES, RATINGS, STUDIOS, YEARS }

data class FilterOption(val id: String, val label: String)

data class FilterSection(val key: SectionKey, val title: String, val options: List<FilterOption>)

data class SortOption(val sortBy: SortBy, val label: String)

val BROWSE_SORT_OPTIONS = listOf(
    SortOption(SortBy.DATE_ADDED, "Date added"),
    SortOption(SortBy.RELEASE_DATE, "Release date"),
    SortOption(SortBy.RELEASE_YEAR, "Release year"),
    SortOption(SortBy.IMDB_RATING, "IMDb rating"),
    SortOption(SortBy.TITLE, "Title"),
    SortOption(SortBy.RANDOM, "Random")
)
