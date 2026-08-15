package zechs.zplex.feature_movies.browse

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.mvi.MviViewModel
import zechs.zplex.common.utils.Result
import zechs.zplex.zplex_api.data.browse.FilterQuery
import zechs.zplex.zplex_api.data.local.PaginatedResponse
import zechs.zplex.zplex_api.data.local.browse.BrowsePreferences
import zechs.zplex.zplex_api.data.local.browse.BrowsePrefsStore
import zechs.zplex.zplex_api.data.local.config.ConfigStore
import zechs.zplex.zplex_api.data.paging.MediaListPagingSource
import zechs.zplex.zplex_api.data.remote.api.MediaListItem
import zechs.zplex.zplex_api.data.remote.api.config.model.filter.Filter
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.enums.OrderBy
import zechs.zplex.zplex_api.data.remote.api.enums.SortBy
import zechs.zplex.zplex_api.utils.ApiConfig.DEFAULT_PAGE_SIZE

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BrowseViewModel(
    val mediaType: MediaType,
    private val configStore: ConfigStore,
    private val prefsStore: BrowsePrefsStore
) : MviViewModel<BrowseState, BrowseAction, BrowseEvent>(BrowseState(mediaType = mediaType)) {

    protected abstract suspend fun fetchPage(
        page: Int,
        sortBy: SortBy,
        orderBy: OrderBy,
        filterBy: String
    ): Result<PaginatedResponse<MediaListItem>>

    private data class Query(val sortBy: SortBy, val orderBy: OrderBy, val filterBy: String)

    private val query = MutableStateFlow(Query(SortBy.DATE_ADDED, OrderBy.DESC, ""))

    val pagingData: Flow<PagingData<MediaListItem>> = query.flatMapLatest { q ->
        Pager(
            config = PagingConfig(pageSize = DEFAULT_PAGE_SIZE, initialLoadSize = DEFAULT_PAGE_SIZE)
        ) {
            MediaListPagingSource { page -> fetchPage(page, q.sortBy, q.orderBy, q.filterBy) }
        }.flow
    }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            val prefs = prefsStore.preferences(mediaType).first()
            setState { copy(sortBy = prefs.sortBy, orderBy = prefs.orderBy, filter = prefs.filter) }
            query.value = Query(prefs.sortBy, prefs.orderBy, prefs.filter.toFilterBy())
        }
        viewModelScope.launch {
            configStore.config.collect { config ->
                val filter = config?.filters?.firstOrNull { it.type == mediaType.filterType }
                setState { copy(sections = filter?.toSections() ?: emptyList()) }
            }
        }
    }

    override fun onAction(action: BrowseAction) {
        when (action) {
            is BrowseAction.SetSort -> {
                setState { copy(sortBy = action.sortBy, orderBy = action.orderBy) }
                query.value = query.value.copy(sortBy = action.sortBy, orderBy = action.orderBy)
                persist()
            }

            is BrowseAction.ApplyFilter -> {
                setState { copy(filter = action.filter) }
                query.value = query.value.copy(filterBy = action.filter.toFilterBy())
                persist()
            }

            BrowseAction.ClearFilter -> {
                setState { copy(filter = FilterQuery()) }
                query.value = query.value.copy(filterBy = "")
                persist()
            }

            is BrowseAction.SelectItem -> setState { copy(selectedItem = action.item) }

            is BrowseAction.OpenDetail ->
                sendEvent(BrowseEvent.NavigateToDetail(mediaType, action.item.tmdbId))
        }
    }

    private fun persist() {
        viewModelScope.launch {
            prefsStore.save(
                mediaType,
                BrowsePreferences(currentState.sortBy, currentState.orderBy, currentState.filter)
            )
        }
    }
}

private val MediaType.filterType: String
    get() = when (this) {
        MediaType.MOVIE -> "movie"
        MediaType.SHOW -> "show"
    }

private fun Filter.toSections(): List<FilterSection> = buildList {
    if (genres.isNotEmpty()) {
        add(
            FilterSection(
                SectionKey.GENRES,
                "Genres",
                genres.map { FilterOption(it.id.toString(), it.name) }
            )
        )
    }
    if (parentalRatings.isNotEmpty()) {
        add(
            FilterSection(
                SectionKey.RATINGS,
                "Parental ratings",
                parentalRatings.map { FilterOption(it, it) }
            )
        )
    }
    if (studios.isNotEmpty()) {
        add(
            FilterSection(
                SectionKey.STUDIOS,
                "Studios",
                studios.map { FilterOption(it.id.toString(), it.name) }
            )
        )
    }
    if (years.isNotEmpty()) {
        add(
            FilterSection(
                SectionKey.YEARS,
                "Years",
                years.map { FilterOption(it.toString(), it.toString()) }
            )
        )
    }
}
