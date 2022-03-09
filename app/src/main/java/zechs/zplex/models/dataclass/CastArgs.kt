package zechs.zplex.models.dataclass

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class CastArgs(
    val creditId: String,
    val personId: Int,
    val name: String,
    val profile_path: String?
) : Serializable