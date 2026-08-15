package zechs.zplex.zplex_api.data.local.downloads

import androidx.room.TypeConverter
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

class DownloadConverters {
    @TypeConverter
    fun toMediaType(value: String): MediaType = MediaType.valueOf(value)

    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toStatus(value: String): DownloadStatus = DownloadStatus.valueOf(value)

    @TypeConverter
    fun fromStatus(value: DownloadStatus): String = value.name
}
