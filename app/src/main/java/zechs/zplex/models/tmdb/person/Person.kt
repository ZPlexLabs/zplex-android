package zechs.zplex.models.tmdb.person

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.Media

@Keep
data class Person(
    val gender: Int?,
    val known_for: List<Media>,
    val name: String?,
    val profile_path: String?
)