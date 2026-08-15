package zechs.zplex.zplex_api.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import zechs.zplex.common.utils.Result
import zechs.zplex.zplex_api.data.local.PaginatedResponse
import zechs.zplex.zplex_api.data.remote.api.MediaListItem

/** Paging3 source backed by the page-numbered browse endpoints. */
class MediaListPagingSource(
    private val fetch: suspend (page: Int) -> Result<PaginatedResponse<MediaListItem>>
) : PagingSource<Int, MediaListItem>() {

    override fun getRefreshKey(state: PagingState<Int, MediaListItem>): Int? =
        state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaListItem> {
        val page = params.key ?: 1
        return when (val result = fetch(page)) {
            is Result.Success -> {
                val response = result.data
                LoadResult.Page(
                    data = response.data,
                    prevKey = if (page <= 1) null else page - 1,
                    nextKey = if (response.pageNumber >= response.pageCount) null else response.pageNumber + 1
                )
            }

            is Result.Error -> LoadResult.Error(BrowseLoadException(result.message))
        }
    }
}

class BrowseLoadException(message: String) : Exception(message)
