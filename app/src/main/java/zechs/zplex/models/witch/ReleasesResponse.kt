package zechs.zplex.models.witch

import androidx.annotation.Keep

@Keep
data class ReleasesResponse(
    val releasesLog: MutableList<ReleasesLog>
)