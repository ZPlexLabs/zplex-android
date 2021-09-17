package zechs.zplex.ui.viewmodel.tmdb

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
import zechs.zplex.models.tmdb.credits.CreditsResponse
import zechs.zplex.models.tmdb.movies.MoviesResponse
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.utils.Resource
import java.io.IOException

class TmdbViewModel(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : AndroidViewModel(app) {

    val movies: MutableLiveData<Resource<MoviesResponse>> = MutableLiveData()
    val credits: MutableLiveData<Resource<CreditsResponse>> = MutableLiveData()

    fun getMovies(movies_id: Int) =
        viewModelScope.launch {
            movies.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = tmdbRepository.getMovies(movies_id)
                    movies.postValue(handleMoviesListResponse(response))
                } else {
                    movies.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)
                when (t) {
                    is IOException -> movies.postValue(Resource.Error("Network Failure"))
                    else -> movies.postValue(
                        Resource.Error(
                            t.message ?: "Something went wrong"
                        )
                    )
                }
            }
        }

    private fun handleMoviesListResponse(response: Response<MoviesResponse>): Resource<MoviesResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun getCredits(movies_id: Int) =
        viewModelScope.launch {
            credits.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = tmdbRepository.getCredits(movies_id)
                    credits.postValue(handleCreditsListResponse(response))
                } else {
                    credits.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)
                when (t) {
                    is IOException -> credits.postValue(Resource.Error("Network Failure"))
                    else -> credits.postValue(
                        Resource.Error(
                            t.message ?: "Something went wrong"
                        )
                    )
                }
            }
        }

    private fun handleCreditsListResponse(response: Response<CreditsResponse>): Resource<CreditsResponse> {
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
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}