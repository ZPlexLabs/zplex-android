package zechs.zplex.models.tmdb.credit

import androidx.annotation.Keep

@Keep
data class CreditResponse(
    val job: String?,
    val person: Person?
)