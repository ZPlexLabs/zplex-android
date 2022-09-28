package zechs.zplex.data.model.tmdb.media

import androidx.annotation.Keep
import zechs.zplex.data.model.tmdb.entities.Video

@Keep
data class Videos(
    val page: Int?,
    val results: List<Video>?,
    val total_pages: Int?,
    val total_results: Int?
)