package zechs.zplex.models.witch

import androidx.annotation.Keep

@Keep
data class ReleasesLog(
    val file: String,
    val folder: String,
    val time: String,
)