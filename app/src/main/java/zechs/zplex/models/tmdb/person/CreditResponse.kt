package zechs.zplex.models.tmdb.person

import androidx.annotation.Keep

@Keep
data class CreditResponse(
    val job: String?,
    val person: Person?
)