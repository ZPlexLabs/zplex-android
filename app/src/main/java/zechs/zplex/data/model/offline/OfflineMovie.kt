package zechs.zplex.data.model.offline

import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.tmdb.media.MovieResponse
import zechs.zplex.data.model.tmdb.media.TvResponse

@Entity(
    tableName = "movies",
    primaryKeys = ["id"]
)
@Keep
data class OfflineMovie(
    val id: Int,
    val json: String,
    val filePath: String
) {
    fun toMovieResponse(): MovieResponse {
        val type = object : TypeToken<MovieResponse>() {}.type
        return Gson().fromJson(json, type)
    }

    fun toMovie(): Movie {
        val type = object : TypeToken<MovieResponse>() {}.type
        val tv: TvResponse = Gson().fromJson(json, type)
        return Movie(
            id = tv.id,
            title = tv.name!!,
            media_type = "movie",
            poster_path = tv.poster_path,
            vote_average = tv.vote_average,
            fileId = null
        )
    }
}