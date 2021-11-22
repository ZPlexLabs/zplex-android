package zechs.zplex.models.tvdb.series

import androidx.annotation.Keep

@Keep
data class Errors(
    val invalidFilters: List<String>?,
    val invalidLanguage: String?,
    val invalidQueryParams: List<String>?
)