package zechs.zplex.feature_search.search

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.mvi.MviViewModel
import zechs.zplex.common.ui.mvi.toUiError
import zechs.zplex.common.utils.Result
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.suggestions.SearchSuggestion
import zechs.zplex.zplex_api.data.remote.api.suggestions.SuggestionType
import zechs.zplex.zplex_api.data.repository.SuggestionsRepository
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val suggestionsRepository: SuggestionsRepository
) : MviViewModel<SearchState, SearchAction, SearchEvent>(SearchState()) {

    private val catalog = MutableStateFlow<List<SearchResultItem>>(emptyList())
    private val queryFlow = MutableStateFlow("")

    init {
        observeQuery()
        loadSuggestions()
    }

    override fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.QueryChanged -> {
                setState { copy(query = action.query) }
                queryFlow.value = action.query
            }
            SearchAction.ClearQuery -> {
                setState { copy(query = "") }
                queryFlow.value = ""
            }
            SearchAction.Retry -> loadSuggestions()
            is SearchAction.OpenDetail ->
                sendEvent(SearchEvent.NavigateToDetail(action.mediaType, action.tmdbId))
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeQuery() {
        queryFlow
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query -> setState { copy(results = filterCatalog(query)) } }
            .launchIn(viewModelScope)
    }

    private fun filterCatalog(query: String): List<SearchResultItem> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return catalog.value.filter { it.title.contains(trimmed, ignoreCase = true) }
    }

    private fun loadSuggestions() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = suggestionsRepository.searchSuggestions()) {
                is Result.Success -> {
                    catalog.value = result.data.map { it.toItem() }
                    setState {
                        copy(
                            isLoading = false,
                            suggestions = catalog.value,
                            results = filterCatalog(query),
                            error = null
                        )
                    }
                }
                is Result.Error -> setState { copy(isLoading = false, error = result.toUiError()) }
            }
        }
    }

    private fun SearchSuggestion.toItem(): SearchResultItem {
        val mediaType = when (type) {
            SuggestionType.movie -> MediaType.MOVIE
            SuggestionType.show -> MediaType.SHOW
        }
        return SearchResultItem(
            tmdbId = tmdbId,
            mediaType = mediaType,
            title = title,
            typeLabel = if (mediaType == MediaType.MOVIE) "Movie" else "Show"
        )
    }
}
