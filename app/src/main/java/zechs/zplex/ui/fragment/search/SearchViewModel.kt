package zechs.zplex.ui.fragment.search


import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.ThisApp
import zechs.zplex.models.tmdb.search.SearchResponse
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.utils.Resource
import java.io.IOException


class SearchViewModel(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : AndroidViewModel(app) {

    val searchList: MutableLiveData<Resource<SearchResponse>> = MutableLiveData()

    fun getSearchList(query: String) = viewModelScope.launch {
        searchList.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = tmdbRepository.getSearch(query, 1)
                searchList.postValue(handleSearchResponse(response))
            } else {
                searchList.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            println(t.stackTrace)
            println(t.message)
            searchList.postValue(
                Resource.Error(
                    if (t is IOException) "Network Failure" else t.message ?: "Something went wrong"
                )
            )
        }
    }

    private fun handleSearchResponse(
        response: Response<SearchResponse>
    ): Resource<SearchResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<ThisApp>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}