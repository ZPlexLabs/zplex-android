package zechs.zplex.models.drive

data class DriveResponse(
    val files: MutableList<File>,
    val nextPageToken: String?
)