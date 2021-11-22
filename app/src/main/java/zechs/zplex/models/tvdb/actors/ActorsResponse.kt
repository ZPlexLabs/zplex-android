package zechs.zplex.models.tvdb.actors

import androidx.annotation.Keep

@Keep
data class ActorsResponse(
    val data: List<Data>?,
    val errors: Errors?
)