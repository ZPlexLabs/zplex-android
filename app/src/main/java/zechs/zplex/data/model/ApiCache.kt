package zechs.zplex.data.model

import androidx.annotation.Keep
import androidx.room.Entity

@Entity(
    tableName = "api_caches",
    primaryKeys = ["id"]
)
@Keep
data class ApiCache(
    val id: String, // tmdbId, imdbId
    val body: String, // json
    val classType: String, // class name
    val expiration: Long, // TTL time 1 week
)
