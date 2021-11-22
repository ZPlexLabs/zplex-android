package zechs.zplex.models.episode

import androidx.annotation.Keep
import zechs.zplex.utils.Constants.TVDB_IMAGE_PATH

@Keep
data class Episode(
    val fileId: String?,
    val fileName: String?,
    val size: Long?,
    val season: Int?,
    val episode: Int?,
    val episodeName: String?,
    val overview: String?,
    val thumbnailPath: String?,
    val seriesId: Int?
) {
    val thumbnailUrl get() = "$TVDB_IMAGE_PATH$thumbnailPath"
}