package zechs.zplex.models.tvdb.actors

data class Errors(
    val invalidFilters: List<String>,
    val invalidLanguage: String,
    val invalidQueryParams: List<String>
)