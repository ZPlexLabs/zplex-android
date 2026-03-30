package zechs.zplex.zplex_api.data.remote.api

data class MediaListItem(
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val imdbRating: Double?,
    val release: String
)