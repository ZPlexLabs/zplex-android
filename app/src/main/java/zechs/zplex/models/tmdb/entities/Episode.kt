package zechs.zplex.models.tmdb.entities

import androidx.annotation.Keep
import zechs.zplex.models.zplex.Episode

@Keep
data class Episode(
    val guest_stars: List<Cast>?,
    val id: Int,
    val name: String?,
    val overview: String?,
    val episode_number: Int,
    val season_number: Int,
    val still_path: String?,
    val fileId: String?,
    val fileName: String?,
    val fileSize: String?
) {
    fun toZplex() = Episode(
        id = id,
        name = name ?: "TBA",
        overview = overview,
        episode_number = episode_number,
        season_number = season_number,
        still_path = still_path,
        file_id = null,
        file_name = null,
        file_size = null
    )
}