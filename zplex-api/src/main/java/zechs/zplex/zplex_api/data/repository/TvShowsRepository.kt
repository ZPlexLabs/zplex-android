package zechs.zplex.zplex_api.data.repository

import zechs.zplex.common.utils.Result
import zechs.zplex.common.utils.SafeApiCaller
import zechs.zplex.zplex_api.data.local.PaginatedResponse
import zechs.zplex.zplex_api.data.remote.api.MediaListItem
import zechs.zplex.zplex_api.data.remote.api.enums.OrderBy
import zechs.zplex.zplex_api.data.remote.api.enums.SortBy
import zechs.zplex.zplex_api.data.remote.api.tvshows.Episode
import zechs.zplex.zplex_api.data.remote.api.tvshows.LatestTvShow
import zechs.zplex.zplex_api.data.remote.api.tvshows.Season
import zechs.zplex.zplex_api.data.remote.api.tvshows.TvShowApi
import zechs.zplex.zplex_api.data.remote.api.tvshows.TvShowDetails
import zechs.zplex.zplex_api.utils.ApiConfig.DEFAULT_PAGE_SIZE
import javax.inject.Inject

class TvShowsRepository @Inject constructor(
    private val api: TvShowApi,
    private val safeApiCaller: SafeApiCaller
) {

    suspend fun latestShows(): Result<List<LatestTvShow>> =
        safeApiCaller.call { api.tvShowsLatest() }

    suspend fun tvShows(
        sortBy: SortBy = SortBy.DATE_ADDED,
        orderBy: OrderBy = OrderBy.DESC,
        filterBy: String = "",
        pageNumber: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        includeNull: Boolean = true
    ): Result<PaginatedResponse<MediaListItem>> =
        safeApiCaller.call {
            api.tvShows(sortBy, orderBy, filterBy, pageNumber, pageSize, includeNull)
        }

    suspend fun tvShowDetails(tmdbId: Int): Result<TvShowDetails> =
        safeApiCaller.call { api.tvShowDetails(tmdbId) }

    suspend fun seasons(tmdbId: Int): Result<List<Season>> =
        safeApiCaller.call { api.seasons(tmdbId) }

    suspend fun episodes(tmdbId: Int, seasonId: Int): Result<List<Episode>> =
        safeApiCaller.call { api.episodes(tmdbId, seasonId) }

}