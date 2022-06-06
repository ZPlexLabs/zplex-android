package zechs.zplex.models.zplex

import androidx.annotation.Keep
import zechs.zplex.utils.AESEncryption

@Keep
data class SeasonResponse(
    val episodes: List<Episode>?,
    val name: String,
    val overview: String?,
    val poster_path: String?,
    val season_number: Int,
    val access_token: String?,
) {
    val accessToken
        get() = access_token?.let { AESEncryption.decrypt(it) }

}