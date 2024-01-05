package zechs.zplex.data.model.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.tmdb.entities.Media

@Entity(tableName = "watched_movies")
@Keep
data class WatchedMovie(
    val tmdbId: Int,
    val name: String,
    val mediaType: String,
    val posterPath: String?,
    val watchedDuration: Long,
    val totalDuration: Long,
    val createdAt: Long,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
) {

    fun watchProgress() = ((watchedDuration.toDouble() / totalDuration) * 100).toInt()

    fun hasFinished() = watchProgress() > 90

    fun toMedia() = Media(
        id = tmdbId,
        media_type = MediaType.movie,
        name = null,
        poster_path = posterPath,
        title = name,
        vote_average = null,
        backdrop_path = null,
        overview = null,
        release_date = null,
        first_air_date = null
    )

}