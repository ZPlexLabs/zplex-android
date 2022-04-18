package zechs.zplex.models.zplex

import androidx.annotation.Keep
import zechs.zplex.utils.AESEncryption
import zechs.zplex.utils.ConverterUtils

@Keep
data class MovieResponse(
    val id: String,
    val name: String,
    val size: Long,
    val access_token: String,
) {
    val humanSize get() = ConverterUtils.getSize(size)
    val accessToken get() = AESEncryption.decrypt(access_token) ?: access_token
}