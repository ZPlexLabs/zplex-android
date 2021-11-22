package zechs.zplex.models.drive

import androidx.annotation.Keep

@Keep
data class DriveResponse(
    val files: MutableList<File>,
    val nextPageToken: String?
)