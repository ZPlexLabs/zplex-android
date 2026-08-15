package zechs.zplex.zplex_api.data.remote.api.movies

import zechs.zplex.zplex_api.data.remote.api.media.Cast
import zechs.zplex.zplex_api.data.remote.api.media.Crew
import zechs.zplex.zplex_api.data.remote.api.media.IdNamePair
import zechs.zplex.zplex_api.data.remote.api.media.Studio

data class MovieDetails(
    val tmdbId: Int,
    val title: String,
    val collectionId: Int?,
    val fileId: String?,
    val imdbId: String?,
    val imdbRating: String?,
    val imdbVotes: Long?,
    val releaseDate: String?,
    val releaseYear: Short?,
    val parentalRating: String?,
    val runtime: Short?,
    val posterPath: String?,
    val backdropPath: String?,
    val logoPath: String?,
    val trailerLink: String?,
    val tagline: String?,
    val plot: String?,
    val director: String?,
    val genres: List<IdNamePair>?,
    val studios: List<Studio>?,
    val casts: List<Cast>?,
    val crews: List<Crew>?,
    val collections: List<IdNamePair>?
)
