package zechs.zplex.models

import androidx.annotation.Keep

@Keep
data class Player(
    val fileId: String,
    val fileName: String,
    val accessToken: String
)
