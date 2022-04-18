package zechs.zplex.models.zplex

import androidx.annotation.Keep

@Keep
data class Episode(
    val id: Int,
    val name: String,
    val overview: String?,
    val episode_number: Int,
    val season_number: Int,
    val still_path: String?,
    val file_id: String?,
    val file_name: String?,
    val file_size: Long?
)