package zechs.zplex.zplex_api.data.remote.api.tvshows

import zechs.zplex.zplex_api.data.remote.api.media.Cast
import zechs.zplex.zplex_api.data.remote.api.media.Crew
import zechs.zplex.zplex_api.data.remote.api.media.IdNamePair
import zechs.zplex.zplex_api.data.remote.api.media.Studio

data class TvShowDetails(
    val tmdbId: Int,
    val title: String,
    val imdbId: String?,
    val imdbRating: String?,
    val imdbVotes: Long?,
    val releaseDate: String?,
    val release: String?,
    val parentalRating: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val logoPath: String?,
    val trailerLink: String?,
    val plot: String?,
    val director: String?,
    val genres: List<IdNamePair>?,
    val studios: List<Studio>?,
    val casts: List<Cast>?,
    val crews: List<Crew>?,
    val latestSeason: Season?
)
