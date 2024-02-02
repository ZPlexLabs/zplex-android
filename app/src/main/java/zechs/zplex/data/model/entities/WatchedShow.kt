package zechs.zplex.data.model.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "watched_shows")
@Keep
data class WatchedShow(
    val tmdbId: Int,
    val name: String,
    val mediaType: String,
    val posterPath: String?,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val watchedDuration: Long,
    val totalDuration: Long,
    val createdAt: Long,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
) {

    fun watchProgress() = ((watchedDuration.toDouble() / totalDuration) * 100).toInt()
    fun hasFinished() = watchProgress() > 90

}