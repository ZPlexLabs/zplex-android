package zechs.zplex.models.tmdb.collection

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Media

@Keep
data class CollectionsResponse(
    val backdrop_path: String?,
    val id: Int,
    val name: String,
    val overview: String,
    val parts: List<Media>,
    val poster_path: String?
)