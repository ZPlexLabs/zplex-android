package zechs.zplex.zplex_api.data.local

data class PaginatedResponse<T>(
    val data: List<T>,
    val pageNumber: Int,
    val pageCount: Int
)