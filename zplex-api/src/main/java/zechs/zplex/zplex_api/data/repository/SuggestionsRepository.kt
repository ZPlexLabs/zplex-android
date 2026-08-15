package zechs.zplex.zplex_api.data.repository

import zechs.zplex.common.utils.Result
import zechs.zplex.common.utils.SafeApiCaller
import zechs.zplex.zplex_api.data.remote.api.suggestions.SearchSuggestion
import zechs.zplex.zplex_api.data.remote.api.suggestions.SuggestionApi
import javax.inject.Inject

class SuggestionsRepository @Inject constructor(
    private val api: SuggestionApi,
    private val safeApiCaller: SafeApiCaller
) {

    suspend fun searchSuggestions(): Result<List<SearchSuggestion>> =
        safeApiCaller.call { api.searchSuggestions() }
}