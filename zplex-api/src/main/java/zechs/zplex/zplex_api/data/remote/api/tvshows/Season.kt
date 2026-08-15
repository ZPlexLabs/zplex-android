package zechs.zplex.zplex_api.data.remote.api.tvshows

import com.squareup.moshi.Json

data class Season(
    val id: Int,
    val name: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "release_year") val releaseYear: Short?,
    @Json(name = "season_number") val seasonNumber: Short?,
    @Json(name = "episodes_count") val episodeCount: Short?
)
