package zechs.zplex.googledrive.data.remote.api.drive.model

data class DriveResponse(
    val drives: List<Drive>,
    val nextPageToken: String?
)

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