package zechs.zplex.models.tmdb.movies

import androidx.annotation.Keep

@Keep
data class MoviesResponse(
    val adult: Boolean?,
    val backdrop_path: String?,
    val belongs_to_collection: Any?,
    val budget: Int?,
    val genres: List<Genre>?,
    val homepage: String?,
    val id: Int?,
    val imdb_id: String?,
    val original_language: String?,
    val original_title: String?,
    val overview: String?,
    val popularity: Double?,
    val poster_path: Any?,
    val production_companies: List<ProductionCompany>?,
    val production_countries: List<ProductionCountry>?,
    val release_date: String?,
    val runtime: Int?,
    val title: String?,
    val video: Boolean?,
    val vote_average: Double?,
)