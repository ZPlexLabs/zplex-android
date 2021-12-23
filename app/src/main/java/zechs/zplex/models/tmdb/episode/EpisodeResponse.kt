package zechs.zplex.models.tmdb.episode

import androidx.annotation.Keep

@Keep
data class EpisodeResponse(
    val episode_number: Int?,
    val guest_stars: List<GuestStar>?,
    val id: Int?,
    val name: String?,
    val overview: String?,
    val season_number: Int?,
    val still_path: String?,
    val vote_average: Double?
)