package zechs.zplex.models.tmdb.credit

import androidx.annotation.Keep

@Keep
data class PersonResponse(
    val biography: String?,
    val birthday: String?,
    val deathday: String?,
    val gender: Int?,
    val name: String?,
    val place_of_birth: String?,
    val profile_path: String
)