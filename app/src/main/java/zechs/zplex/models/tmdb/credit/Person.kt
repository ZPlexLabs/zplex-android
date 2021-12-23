package zechs.zplex.models.tmdb.credit

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Media

@Keep
data class Person(
    val gender: Int?,
    val known_for: List<Media>,
    val name: String?,
    val profile_path: String?
)