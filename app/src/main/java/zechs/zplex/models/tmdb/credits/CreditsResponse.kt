package zechs.zplex.models.tmdb.credits

import androidx.annotation.Keep

@Keep
data class CreditsResponse(
    val cast: List<Cast>?,
    val crew: List<Crew>?,
    val id: Int?
)