package zechs.zplex.data.model.drive

data class FileResponse(
    val id: String,
    val md5Checksum: String,
    val mimeType: String,
    val modifiedTime: String,
    val name: String,
    val size: String
)