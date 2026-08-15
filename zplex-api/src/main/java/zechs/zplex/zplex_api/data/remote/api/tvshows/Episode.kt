package zechs.zplex.zplex_api.data.remote.api.tvshows

import com.squareup.moshi.Json

data class Episode(
    val id: Int,
    val title: String?,
    val episodeNumber: Short?,
    val seasonNumber: Short?,
    val overview: String?,
    val runtime: Short?,
    @Json(name = "release_date") val releaseDate: String?,
    val stillPath: String?,
    val fileId: String?
)
