package zechs.zplex.models.tmdb.tv

import androidx.annotation.Keep

@Keep
data class Videos(
    val page: Int?,
    val results: List<Video>?,
    val total_pages: Int?,
    val total_results: Int?
)