package zechs.zplex.data.model.drive

import androidx.annotation.Keep
import zechs.zplex.utils.util.Converter

@Keep
data class DriveFile(
    val id: String,
    val name: String,
    val size: Long?,
    val mimeType: String,
    val iconLink: String?,
    val modifiedTime: Long?,
    val shortcutDetails: ShortcutDetails
) {
    val humanSize = size?.let { Converter.toHumanSize(it) }

    val isVideoFile = mimeType.startsWith("video/")

    val isFolder = mimeType == "application/vnd.google-apps.folder"
            || mimeType == "drive#drive"

    val isShortcut = mimeType == "application/vnd.google-apps.shortcut"

    val isShortcutFolder = shortcutDetails.targetMimeType == "application/vnd.google-apps.folder"

    val isShortcutVideo = shortcutDetails.targetMimeType?.startsWith("video/") ?: false

    val iconLink128 = iconLink?.replace("16", "128")
}