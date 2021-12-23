package zechs.zplex.models.tmdb.search

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Media

@Keep
data class SearchResponse(
    val page: Int,
    val results: MutableList<Media>,
    val total_pages: Int,
    val total_results: Int
)