package zechs.zplex.zplex_api.data.remote.api.suggestions

data class SuggestionMediaItem(
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val type: SuggestionType,
    val release: String
)

