package zechs.zplex.zplex_api.data.local.downloads

import androidx.room.Entity
import androidx.room.PrimaryKey
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val fileId: String,
    val tmdbId: Int,
    val mediaType: MediaType,
    val title: String,
    val subtitle: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val posterPath: String?,
    val status: DownloadStatus,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val filePath: String?,
    val errorMessage: String?,
    val createdAt: Long,
    val updatedAt: Long
)
