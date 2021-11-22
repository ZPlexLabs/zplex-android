package zechs.zplex.models.episode

import androidx.annotation.Keep

@Keep
data class Data(
    val airedEpisodeNumber: Int?,
    val airedSeason: Int?,
    val episodeName: String?,
    val filename: String?,
    val overview: String?,
    val seriesId: Int?
)