package zechs.zplex.models.tmdb.discover.movie

import androidx.annotation.Keep

@Keep
data class DiscoverMovieResponse(
    val page: Int?,
    val results: List<Result>?,
    val total_pages: Int?,
    val total_results: Int?
)