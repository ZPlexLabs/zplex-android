package zechs.zplex.models.tvdb.actors

import androidx.annotation.Keep

@Keep
data class Data(
    val id: Int?,
    val image: String?,
    val imageAdded: String?,
    val imageAuthor: Int?,
    val lastUpdated: String?,
    val name: String?,
    val role: String?,
    val seriesId: Int?,
    val sortOrder: Int?
)