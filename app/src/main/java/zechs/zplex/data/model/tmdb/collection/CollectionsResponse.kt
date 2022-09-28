package zechs.zplex.data.model.tmdb.collection

import androidx.annotation.Keep
import zechs.zplex.data.model.tmdb.entities.Media

@Keep
data class CollectionsResponse(
    val id: Int,
    val name: String,
    val overview: String?,
    val parts: List<Media>,
    val poster_path: String?,
    val backdrop_path: String?
)