package zechs.zplex.data.model.drive

import androidx.annotation.Keep
import java.time.Instant
import java.time.format.DateTimeFormatter

@Keep
data class FilesResponse(
    val files: List<File>,
    val nextPageToken: String?
)

@Keep
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

@Keep
data class ShortcutDetails(
    val targetId: String? = null,
    val targetMimeType: String? = null
)