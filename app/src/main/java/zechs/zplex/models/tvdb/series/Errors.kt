package zechs.zplex.models.tvdb.series

data class Errors(
    val invalidFilters: List<String>?,
    val invalidLanguage: String?,
    val invalidQueryParams: List<String>?
)