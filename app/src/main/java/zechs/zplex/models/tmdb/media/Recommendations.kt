package zechs.zplex.models.tmdb.media

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Media

@Keep
data class Recommendations(
    val page: Int?,
    val results: List<Media>?,
    val total_pages: Int?,
    val total_results: Int?
)