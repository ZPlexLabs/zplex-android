package zechs.zplex.data.model.tmdb.keyword

import androidx.annotation.Keep

@Keep
data class KeywordResponse(
    val page: Int,
    val results: List<TmdbKeyword>,
    val total_pages: Int,
    val total_results: Int
)