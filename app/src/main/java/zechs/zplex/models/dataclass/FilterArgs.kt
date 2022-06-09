package zechs.zplex.models.dataclass

import zechs.zplex.models.enum.MediaType
import zechs.zplex.models.enum.Order
import zechs.zplex.models.enum.SortBy
import zechs.zplex.models.tmdb.keyword.TmdbKeyword


data class FilterArgs(
    val mediaType: MediaType,
    val sortBy: SortBy,
    val order: Order,
    val page: Int,
    val withKeyword: List<TmdbKeyword>?,
    val withGenres: Int?
)