package zechs.zplex.data.model.tmdb.entities

import androidx.annotation.Keep

@Keep
data class Episode(
    val id: Int,
    val name: String?,
    val overview: String?,
    val episode_number: Int,
    val season_number: Int,
    val still_path: String?,
    val guest_stars: List<Cast>?,
    val fileId: String?,
    val fileSize: String?,
    val offline: Boolean = false,
    val progress: Int = 0
)