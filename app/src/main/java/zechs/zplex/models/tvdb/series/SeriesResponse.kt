package zechs.zplex.models.tvdb.series

import androidx.annotation.Keep

@Keep
data class SeriesResponse(
    val data: Data?,
    val errors: Errors?
)