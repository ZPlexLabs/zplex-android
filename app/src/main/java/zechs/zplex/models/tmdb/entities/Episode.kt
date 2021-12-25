package zechs.zplex.models.tmdb.entities

import androidx.annotation.Keep

@Keep
data class Episode(
    val episode_number: Int?,
    val guest_stars: List<Cast>,
    val id: Int,
    val name: String?,
    val overview: String?,
    val season_number: Int?,
    val still_path: String?,
    val vote_average: Double?
)