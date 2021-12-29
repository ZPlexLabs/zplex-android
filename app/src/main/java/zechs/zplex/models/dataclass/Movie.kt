package zechs.zplex.models.dataclass

import androidx.annotation.Keep
import androidx.room.Entity

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
)