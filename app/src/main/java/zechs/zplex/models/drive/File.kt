package zechs.zplex.models.drive

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "files"
)
data class File(
    @PrimaryKey(autoGenerate = true)
    var serial: Int? = null,
    val id: String,
    val name: String,
    val size: Long,
    val thumbnailLink: String?
) : Serializable