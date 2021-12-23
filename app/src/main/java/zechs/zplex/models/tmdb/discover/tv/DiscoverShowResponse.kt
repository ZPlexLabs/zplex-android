package zechs.zplex.models.tmdb.discover.tv

import androidx.annotation.Keep

@Keep
data class DiscoverShowResponse(
    val page: Int?,
    val results: List<Result>?,
    val total_pages: Int?,
    val total_results: Int?
)