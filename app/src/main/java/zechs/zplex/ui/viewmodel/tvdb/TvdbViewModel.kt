package zechs.zplex.ui.viewmodel.tvdb

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
import zechs.zplex.models.tvdb.actors.ActorsResponse
import zechs.zplex.models.tvdb.series.SeriesResponse
import zechs.zplex.repository.TvdbRepository
import zechs.zplex.utils.Resource
import java.io.IOException

class TvdbViewModel(
    app: Application,
    private val tvdbRepository: TvdbRepository
) : AndroidViewModel(app) {

    val series: MutableLiveData<Resource<SeriesResponse>> = MutableLiveData()
    val actors: MutableLiveData<Resource<ActorsResponse>> = MutableLiveData()

    fun getSeries(series_id: Int) =
        viewModelScope.launch {
            series.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = tvdbRepository.getSeries(series_id)
                    series.postValue(handleSeriesListResponse(response))
                } else {
                    series.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)
                when (t) {
                    is IOException -> series.postValue(Resource.Error("Network Failure"))
                    else -> series.postValue(
                        Resource.Error(
                            t.message ?: "Something went wrong"
                        )
                    )
                }
            }
        }

    private fun handleSeriesListResponse(response: Response<SeriesResponse>): Resource<SeriesResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun getActor(series_id: Int) =
        viewModelScope.launch {
            actors.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = tvdbRepository.getActors(series_id)
                    actors.postValue(handleActorsListResponse(response))
                } else {
                    actors.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)
                when (t) {
                    is IOException -> actors.postValue(Resource.Error("Network Failure"))
                    else -> actors.postValue(
                        Resource.Error(
                            t.message ?: "Something went wrong"
                        )
                    )
                }
            }
        }

    private fun handleActorsListResponse(response: Response<ActorsResponse>): Resource<ActorsResponse> {
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