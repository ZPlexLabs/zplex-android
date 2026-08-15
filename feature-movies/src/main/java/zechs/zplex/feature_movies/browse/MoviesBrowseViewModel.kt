package zechs.zplex.feature_movies.browse

import dagger.hilt.android.lifecycle.HiltViewModel
import zechs.zplex.common.utils.Result
import zechs.zplex.zplex_api.data.local.PaginatedResponse
import zechs.zplex.zplex_api.data.local.browse.BrowsePrefsStore
import zechs.zplex.zplex_api.data.local.config.ConfigStore
import zechs.zplex.zplex_api.data.remote.api.MediaListItem
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.enums.OrderBy
import zechs.zplex.zplex_api.data.remote.api.enums.SortBy
import zechs.zplex.zplex_api.data.repository.MoviesRepository
import javax.inject.Inject

@HiltViewModel
class MoviesBrowseViewModel @Inject constructor(
    private val repository: MoviesRepository,
    configStore: ConfigStore,
    prefsStore: BrowsePrefsStore
) : BrowseViewModel(MediaType.MOVIE, configStore, prefsStore) {

    override suspend fun fetchPage(
        page: Int,
        sortBy: SortBy,
        orderBy: OrderBy,
        filterBy: String
    ): Result<PaginatedResponse<MediaListItem>> =
        repository.movies(sortBy, orderBy, filterBy, page)
}
