package zechs.zplex.data.model.offline

import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import zechs.zplex.data.model.entities.Show
import zechs.zplex.data.model.tmdb.media.TvResponse

@Entity(
    tableName = "shows",
    primaryKeys = ["id"]
)
@Keep
data class OfflineShow(
    val id: Int,
    val json: String,
) {
    fun toTvResponse(): TvResponse {
        val type = object : TypeToken<TvResponse>() {}.type
        return Gson().fromJson(json, type)
    }

    fun toShow(): Show {
        val type = object : TypeToken<TvResponse>() {}.type
        val tv: TvResponse = Gson().fromJson(json, type)
        return Show(
            id = tv.id,
            name = tv.name!!,
            media_type = "tv",
            poster_path = tv.poster_path,
            vote_average = tv.vote_average,
            fileId = null
        )
    }
}