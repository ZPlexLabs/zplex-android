package zechs.zplex.models.zplex

import androidx.annotation.Keep
import com.squareup.moshi.Json
import zechs.zplex.utils.ConverterUtils

@Keep
data class MovieResponse(
    val id: String,
    val name: String,
    val size: Long,
    @Json(name = "access_token")
    val accessToken: String,
) {
    val humanSize get() = ConverterUtils.getSize(size)
}