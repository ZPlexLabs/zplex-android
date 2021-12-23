package zechs.zplex.models.season

import androidx.annotation.Keep

@Keep
data class Ep(
    val id: Int?,
    val episode_number: Int?,
    val name: String?,
    val overview: String?,
    val season_number: Int?,
    val still_path: String?,
    val fileId: String,
    val fileName: String,
    val fileSize: String?
)