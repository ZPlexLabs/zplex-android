package zechs.zplex.googledrive.data.remote.api.drive.model

import java.time.Instant
import java.time.format.DateTimeFormatter


data class FilesResponse(
    val files: List<File>,
    val nextPageToken: String?
)

data class File(
    val id: String,
    val name: String,
    val size: Long?,
    val iconLink: String,
    val mimeType: String,
    val modifiedTime: String?,
    val shortcutDetails: ShortcutDetails = ShortcutDetails(),
) {

    private fun convertToEpoch(): Long? {
        if (modifiedTime.isNullOrEmpty()) return null
        return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(modifiedTime)).toEpochMilli()
    }

    fun toDriveFile() =
        DriveFile(id, name, size, mimeType, iconLink, convertToEpoch(), shortcutDetails)
}


data class ShortcutDetails(
    val targetId: String? = null,
    val targetMimeType: String? = null
)