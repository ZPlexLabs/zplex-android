package zechs.zplex.ui.fragment.browse

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.ThisApp
import zechs.zplex.models.dataclass.FilterArgs
import zechs.zplex.models.tmdb.search.SearchResponse
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.utils.Resource
import java.io.IOException

class BrowseViewModel(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : AndroidViewModel(app) {

    val browse: MutableLiveData<Resource<SearchResponse>> = MutableLiveData()
    private var browseResponse: SearchResponse? = null
    private var page = 1

    private var newSearchQuery: String? = null
    private var oldSearchQuery: String? = null

    fun getBrowse(filterArgs: FilterArgs) = viewModelScope.launch {
        newSearchQuery = filterArgs.withGenres.toString()
        browse.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = tmdbRepository.getBrowse(
                    mediaType = filterArgs.mediaType,
                    sortBy = filterArgs.sortBy,
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

//    private fun handleBrowseResponse(
//        response: Response<SearchResponse>
//    ): Resource<SearchResponse> {
//        if (response.isSuccessful) {
//            response.body()?.let { resultResponse ->
//                return Resource.Success(resultResponse)
//            }
//        }
//        return Resource.Error(response.message())
//    }


    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<ThisApp>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}