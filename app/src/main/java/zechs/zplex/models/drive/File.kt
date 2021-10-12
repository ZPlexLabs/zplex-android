package zechs.zplex.models.drive

import androidx.room.Entity
import androidx.room.PrimaryKey
import zechs.zplex.utils.ConverterUtils
import java.io.Serializable

@Entity(
    tableName = "files"
)
data class File(
    @PrimaryKey(autoGenerate = true)
    var serial: Int? = null,
    val id: String,
    val name: String,
    val size: Long?
) : Serializable {
    val humanSize get() = ConverterUtils.getSize(size ?: 0)
}