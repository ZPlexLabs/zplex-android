package zechs.zplex.models

import androidx.annotation.Keep
import zechs.zplex.models.drive.File

@Keep
data class Args(
    val file: File,
    val mediaId: Int,
    val type: String,
    val name: String
)