package zechs.zplex.zplex_api.data.remote.api.suggestions

import retrofit2.Response
import retrofit2.http.GET

interface SuggestionApi {

    @GET("/api/suggestion/search")
    suspend fun searchSuggestions(): Response<List<SearchSuggestion>>
}
