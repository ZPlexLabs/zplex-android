package zechs.zplex.zplex_api.data.remote.api.tvshows

data class LatestTvShow(
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val episodeCount: Int,
    val release: String
)