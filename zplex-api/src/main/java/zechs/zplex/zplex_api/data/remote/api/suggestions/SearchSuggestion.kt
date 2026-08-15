package zechs.zplex.zplex_api.data.remote.api.suggestions

data class SearchSuggestion(
    val tmdbId: Int,
    val title: String,
    val type: SuggestionType
)
