package zechs.zplex.data.model.entities

import androidx.annotation.Keep
import androidx.room.Entity
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.tmdb.entities.Media

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
    val vote_average: Double?,
    val fileId: String?,
    val modifiedTime: Long? = null
) {

    fun toMedia() = Media(
        id = id,
        media_type = MediaType.tv,
        name = name,
        poster_path = poster_path,
        title = null,
        vote_average = vote_average,
        backdrop_path = null,
        overview = null,
        release_date = null,
        first_air_date = null,
        fileId = fileId
    )

}