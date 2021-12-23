package zechs.zplex.models.tmdb.tv

import androidx.annotation.Keep

@Keep
data class Season(
    val episode_count: Int,
    val id: Int,
    val name: String,
    val poster_path: String?,
    val season_number: Int
)