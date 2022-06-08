package zechs.zplex.ui.fragment.browse

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.models.dataclass.FilterArgs
import zechs.zplex.models.tmdb.keyword.KeywordResponse
import zechs.zplex.models.tmdb.keyword.TmdbKeyword
import zechs.zplex.models.tmdb.search.SearchResponse
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.ui.fragment.browse.BrowseFragment.Companion.TAG
import zechs.zplex.utils.Event
import zechs.zplex.utils.Resource
import java.io.IOException

class BrowseViewModel(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : BaseAndroidViewModel(app) {

    private val _keywordsList = MutableLiveData<Event<List<TmdbKeyword>>>()
    val keywordsList: LiveData<Event<List<TmdbKeyword>>>
        get() = _keywordsList

    val browse: MutableLiveData<Resource<SearchResponse>> = MutableLiveData()
    private var browseResponse: SearchResponse? = null
    private var page = 1

    private var newSearchQuery: FilterArgs? = null
    private var oldSearchQuery: FilterArgs? = null

    fun getBrowse(filterArgs: FilterArgs) = viewModelScope.launch {
        newSearchQuery = filterArgs
        Log.d(
            TAG,
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
        } catch (e: Exception) {
            Log.d(TAG, e.message.toString())

            if (e is IOException) {
                browse.postValue(Resource.Error("Network Failure"))
            } else {
                browse.postValue(
                    Resource.Error(e.localizedMessage ?: "Something went wrong")
                )
            }
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

    fun getSearch(query: String) = viewModelScope.launch {
        try {
            if (hasInternetConnection()) {
                val response = tmdbRepository.searchKeyword(query)
                _keywordsList.postValue(Event(handleKeywordResponse(response)))
            } else {
                _keywordsList.postValue(Event(listOf()))
            }
        } catch (e: Exception) {
            Log.d(TAG, e.message.toString())
            _keywordsList.postValue(Event(listOf()))
        }
    }

    fun clearKeywordList() = viewModelScope.launch {
        _keywordsList.postValue(Event(listOf()))
    }

    private fun handleKeywordResponse(
        response: Response<KeywordResponse>
    ): List<TmdbKeyword> {
        if (response.isSuccessful) {
            response.body()?.let { return it.results }
        }
        return listOf()
    }

}