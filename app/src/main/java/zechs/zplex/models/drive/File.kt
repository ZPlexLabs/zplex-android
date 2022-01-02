package zechs.zplex.models.drive

import androidx.annotation.Keep
import zechs.zplex.utils.ConverterUtils
import java.io.Serializable

@Keep
data class File(
    val id: String,
    val name: String,
    val size: Long?
) : Serializable {
    val humanSize get() = ConverterUtils.getSize(size ?: 0)
}