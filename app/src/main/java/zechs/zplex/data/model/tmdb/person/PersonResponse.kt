package zechs.zplex.data.model.tmdb.person

import androidx.annotation.Keep
import zechs.zplex.utils.util.Converter

@Keep
data class PersonResponse(
    val combined_credits: CombinedCredits,
    val id: Int,
    val imdb_id: String?,
    val biography: String?,
    val birthday: String?,
    val deathday: String?,
    val gender: Int?,
    val name: String?,
    val place_of_birth: String?,
    val profile_path: String?
) {
    val genderName
        get() = when (gender) {
            0 -> "Others"
            1 -> "Female"
            2 -> "Male"
            else -> "Unknown"
        }

    fun age(): Int? {
        return if (birthday != null) {
            if (deathday != null) {
                Converter.yearsBetween(birthday, deathday)
            } else {
                Converter.yearsBetween(birthday, Converter.getDate())
            }
        } else null
    }
}