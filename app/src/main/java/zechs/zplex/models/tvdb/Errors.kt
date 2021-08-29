package zechs.zplex.models.tvdb

data class Errors(
    val invalidFilters: List<String>?,
    val invalidLanguage: String?,
    val invalidQueryParams: List<String>?
)