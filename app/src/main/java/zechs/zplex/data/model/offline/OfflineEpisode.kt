package zechs.zplex.data.model.offline

import androidx.annotation.Keep
import androidx.room.Entity

@Entity(
    tableName = "episodes",
    primaryKeys = ["filePath"]
)
@Keep
data class OfflineEpisode(
    val tmdbId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val filePath: String
)