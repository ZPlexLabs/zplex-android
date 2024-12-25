package zechs.zplex.data.model.offline

import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import zechs.zplex.data.model.tmdb.season.SeasonResponse

@Entity(
    tableName = "seasons",
    primaryKeys = ["tmdbId"]
)
@Keep
data class OfflineSeason(
    val tmdbId: Int,
    val seasonNumber: Int,
    val json: String,
) {
    fun toSeasonResponse(): SeasonResponse {
        val type = object : TypeToken<SeasonResponse>() {}.type
        return Gson().fromJson(json, type)
    }
}