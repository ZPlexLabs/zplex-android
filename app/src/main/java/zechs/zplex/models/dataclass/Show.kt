package zechs.zplex.models.dataclass

import androidx.annotation.Keep
import androidx.room.Entity
import zechs.zplex.models.tmdb.entities.Media

@Entity(
    tableName = "shows",
    primaryKeys = ["id"]
)
@Keep
data class Show(
    val id: Int,
    val name: String,
    val media_type: String?,
    val poster_path: String?,
    val vote_average: Double?
) {
    fun toMedia() = Media(
        id = id,
        media_type = "tv",
        name = name,
        poster_path = poster_path,
        title = null,
        vote_average = vote_average,
        backdrop_path = null,
        overview = null,
        release_date = null,
        first_air_date = null
    )
}