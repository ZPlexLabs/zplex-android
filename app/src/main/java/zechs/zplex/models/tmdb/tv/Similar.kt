package zechs.zplex.models.tmdb.tv

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.Media

@Keep
data class Similar(
    val page: Int?,
    val results: List<Media>?,
    val total_pages: Int?,
    val total_results: Int?
)