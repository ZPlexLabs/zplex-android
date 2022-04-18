package zechs.zplex.models.drive

import androidx.annotation.Keep

@Keep
data class DashResponse(
    val error: String?,
    val streams: List<Stream>
)

