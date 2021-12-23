package zechs.zplex.models.dataclass

import androidx.annotation.Keep

@Keep
data class CastArgs(
    val creditId: String,
    val personId: Int
)