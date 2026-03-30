package zechs.zplex.zplex_api.data.remote.api.movies

data class LatestMovie(
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val release: String
)