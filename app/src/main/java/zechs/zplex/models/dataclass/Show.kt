package zechs.zplex.models.dataclass

import androidx.annotation.Keep
import androidx.room.Entity

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
)