package zechs.zplex.data.model.drive

import androidx.annotation.Keep

@Keep
data class DriveResponse(
    val drives: List<Drive>,
    val nextPageToken: String?
)

@Keep
data class Drive(
    val id: String,
    val kind: String,
    val name: String
) {
    fun toDriveFile() = DriveFile(
        id = id,
        name = name,
        size = null,
        mimeType = kind,
        iconLink = null,
        shortcutDetails = ShortcutDetails(),
        modifiedTime = null
    )
}