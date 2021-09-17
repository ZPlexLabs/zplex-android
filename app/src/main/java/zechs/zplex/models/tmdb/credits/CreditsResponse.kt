package zechs.zplex.models.tmdb.credits

data class CreditsResponse(
    val cast: List<Cast>?,
    val crew: List<Crew>?,
    val id: Int?
)