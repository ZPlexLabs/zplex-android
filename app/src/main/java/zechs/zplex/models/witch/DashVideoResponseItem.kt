package zechs.zplex.models.witch

import androidx.annotation.Keep
import zechs.zplex.utils.ConverterUtils

@Keep
data class DashVideoResponseItem(
    val url: String,
    val resolution: String,
    val quality: String,
    val size: Long,
    val drive_stream: String
) {
    val humanSize get() = ConverterUtils.getSize(size)
}