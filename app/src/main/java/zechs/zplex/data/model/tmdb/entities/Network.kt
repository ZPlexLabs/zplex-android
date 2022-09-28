package zechs.zplex.data.model.tmdb.entities

import androidx.annotation.Keep

@Keep
data class Network(
    val id: Int,
    val logo_path: String?,
    val name: String?,
    val origin_country: String?
)