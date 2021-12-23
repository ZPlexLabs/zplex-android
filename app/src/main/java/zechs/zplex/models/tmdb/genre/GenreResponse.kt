package zechs.zplex.models.tmdb.genre

import androidx.annotation.Keep

@Keep
data class GenreResponse(
    val genres: List<Genre>
)