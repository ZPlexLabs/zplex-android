package zechs.zplex.models.tmdb.movies

import androidx.annotation.Keep

@Keep
data class ProductionCountry(
    val iso_3166_1: String?,
    val name: String?
)