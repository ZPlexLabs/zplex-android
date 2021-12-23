package zechs.zplex.models.tmdb.entities

import androidx.annotation.Keep

@Keep
data class Cast(
    val character: String,
    val credit_id: String,
    val id: Int,
    val name: String,
    val profile_path: String?
)