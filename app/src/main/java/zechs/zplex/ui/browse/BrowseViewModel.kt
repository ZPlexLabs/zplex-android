package zechs.zplex.ui.browse

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.data.model.tmdb.keyword.KeywordResponse
import zechs.zplex.data.model.tmdb.keyword.TmdbKeyword
import zechs.zplex.data.model.tmdb.search.SearchResponse
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.ui.browse.BrowseFragment.Companion.TAG
import zechs.zplex.ui.shared_viewmodels.FilterArgs
import zechs.zplex.utils.state.Event
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.state.ResourceExt.Companion.postError
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : BaseAndroidViewModel(app) {

    private val _keywordsList = MutableLiveData<Event<List<TmdbKeyword>>>()
    val keywordsList: LiveData<Event<List<TmdbKeyword>>>
        get() = _keywordsList

    private val _browse = MutableLiveData<Resource<SearchResponse>>()
    val browse: LiveData<Resource<SearchResponse>>
        get() = _browse

    private var browseResponse: SearchResponse? = null
    private var page = 1

    private var newSearchQuery: FilterArgs? = null
    private var oldSearchQuery: FilterArgs? = null

    var isLoading = false

    var isLastPage = false
        private set

    fun getBrowse(
        filterArgs: FilterArgs
    ) = viewModelScope.launch(Dispatchers.IO) {
        newSearchQuery = filterArgs

        val debug = "usedPage=${if (newSearchQuery != oldSearchQuery) 1 else page}, " +
                "oldSearchQuery=$oldSearchQuery, " +
                "newSearchQuery=$newSearchQuery, page=$page"
        Log.d(TAG, debug)

        _browse.postValue(Resource.Loading())
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
                _browse.postValue(handleBrowseResponse(response))
            } else {
                _browse.postValue(Resource.Error("No internet connection"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _browse.postValue(postError(e))
        }
    }

    private fun handleBrowseResponse(
        response: Response<SearchResponse>
    ): Resource<SearchResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                page++
                isLastPage = resultResponse.page == resultResponse.total_pages

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

    fun getSearch(
        query: String
    ) = viewModelScope.launch(Dispatchers.IO) {
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