package zechs.zplex.zplex_api.data.repository

import zechs.zplex.common.utils.SafeApiCaller
import zechs.zplex.zplex_api.data.local.PaginatedResponse
import zechs.zplex.zplex_api.data.remote.api.MediaListItem
import zechs.zplex.zplex_api.data.remote.api.enums.OrderBy
import zechs.zplex.zplex_api.data.remote.api.enums.SortBy
import zechs.zplex.zplex_api.data.remote.api.movies.LatestMovie

import zechs.zplex.zplex_api.data.remote.api.movies.MovieApi
import zechs.zplex.zplex_api.utils.ApiConfig.DEFAULT_PAGE_SIZE
import javax.inject.Inject
import zechs.zplex.common.utils.Result

class MoviesRepository @Inject constructor(
    private val api: MovieApi,
    private val safeApiCaller: SafeApiCaller
) {

    suspend fun latestShows(): Result<List<LatestMovie>> =
        safeApiCaller.call { api.moviesLatest() }

    suspend fun movies(
        sortBy: SortBy = SortBy.DATE_ADDED,
        orderBy: OrderBy = OrderBy.DESC,
        filterBy: String = "",
        pageNumber: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        includeNull: Boolean = true
    ): Result<PaginatedResponse<MediaListItem>> =
        safeApiCaller.call {
            api.movies(sortBy, orderBy, filterBy, pageNumber, pageSize, includeNull)
        }
}