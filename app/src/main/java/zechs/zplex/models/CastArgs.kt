package zechs.zplex.models

import androidx.annotation.Keep

@Keep
data class CastArgs(
    val creditId: String,
    val personId: Int
)