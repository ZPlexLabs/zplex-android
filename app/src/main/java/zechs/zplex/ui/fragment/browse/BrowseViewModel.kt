package zechs.zplex.ui.fragment.browse

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.models.dataclass.FilterArgs
import zechs.zplex.models.tmdb.search.SearchResponse
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.utils.Resource
import java.io.IOException

class BrowseViewModel(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : BaseAndroidViewModel(app) {

    val browse: MutableLiveData<Resource<SearchResponse>> = MutableLiveData()
    private var browseResponse: SearchResponse? = null
    private var page = 1

    private var newSearchQuery: FilterArgs? = null
    private var oldSearchQuery: FilterArgs? = null

    fun getBrowse(filterArgs: FilterArgs) = viewModelScope.launch {
        newSearchQuery = filterArgs
        println(
            "usedPage=${if (newSearchQuery != oldSearchQuery) 1 else page}, " +
                    "oldSearchQuery=$oldSearchQuery, " +
                    "newSearchQuery=$newSearchQuery, page=$page"
        )
        browse.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = tmdbRepository.getBrowse(
                    mediaType = filterArgs.mediaType,
                    sortBy = filterArgs.sortBy,
                    order = filterArgs.order,
                    page = if (newSearchQuery != oldSearchQuery) 1 else page,
                    withKeyword = filterArgs.withKeyword,
                    withGenres = filterArgs.withGenres
                )
                browse.postValue(handleBrowseResponse(response))
            } else {
                browse.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            println(t.stackTrace)
            println(t.message)
            browse.postValue(
                Resource.Error(
                    if (t is IOException) "Network Failure" else t.message ?: "Something went wrong"
                )
            )
        }
    }

    private fun handleBrowseResponse(
        response: Response<SearchResponse>
    ): Resource<SearchResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                page++
                if (browseResponse == null || newSearchQuery != oldSearchQuery) {
                    page = 2
                    oldSearchQuery = newSearchQuery
                    browseResponse = resultResponse
                } else {
                    val oldItems = browseResponse?.results
                    val newItems = resultResponse.results
                    oldItems?.addAll(newItems)
                }
                return Resource.Success(browseResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


}