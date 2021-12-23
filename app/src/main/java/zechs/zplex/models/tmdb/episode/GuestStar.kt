package zechs.zplex.models.tmdb.episode

import androidx.annotation.Keep

@Keep
data class GuestStar(
    val character: String,
    val credit_id: String,
    val name: String,
    val id: Int,
    val profile_path: String?
)