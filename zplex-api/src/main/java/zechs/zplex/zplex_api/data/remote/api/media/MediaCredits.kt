package zechs.zplex.zplex_api.data.remote.api.media

import com.squareup.moshi.Json

data class IdNamePair(
    val id: Int,
    val name: String
)

data class Cast(
    val name: String?,
    val role: String?,
    val image: String?,
    val gender: String?
)

data class Crew(
    val name: String?,
    val job: String?,
    val image: String?
)

data class Studio(
    val id: String?,
    val name: String?,
    @Json(name = "logo_path") val logoPath: String?,
    @Json(name = "origin_country") val originCountry: String?
)
