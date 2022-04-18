package zechs.zplex.models.drive

import androidx.annotation.Keep

@Keep
data class Stream(
    val resolution: String,
    val url: String,
    val quality: String,
    val driveStream: String
)