package zechs.zplex.data.model.entities

import androidx.annotation.Keep
import androidx.room.Entity
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.tmdb.entities.Media

@Entity(
    tableName = "movies",
    primaryKeys = ["id"]
)
@Keep
data class Movie(
    val id: Int,
    val title: String,
    val media_type: String?,
    val poster_path: String?,
    val vote_average: Double?,
    val fileId: String?
) {

    fun toMedia() = Media(
        id = id,
        media_type = MediaType.movie,
        name = null,
        poster_path = poster_path,
        title = title,
        vote_average = vote_average,
        backdrop_path = null,
        overview = null,
        release_date = null,
        first_air_date = null
    )

}