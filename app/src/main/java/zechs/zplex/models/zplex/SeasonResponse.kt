package zechs.zplex.models.zplex

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SeasonResponse(
    val episodes: List<Episode>?,
    val name: String,
    val overview: String?,
    val poster_path: String?,
    val season_number: Int,
    @Json(name = "access_token")
    val accessToken: String?
)