package zechs.zplex.data.model.tmdb.search

import androidx.annotation.Keep
import zechs.zplex.data.model.tmdb.entities.Media

@Keep
data class SearchResponse(
    val page: Int,
    val results: MutableList<Media>,
    val total_pages: Int,
    val total_results: Int
)