package zechs.zplex.ui.viewmodel.release_log

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
import zechs.zplex.models.witch.ReleasesResponse
import zechs.zplex.repository.ReleasesRepository
import zechs.zplex.utils.Resource
import java.io.IOException

class ReleaseLogViewModel(
    app: Application,
    private val releasesRepository: ReleasesRepository
) : AndroidViewModel(app) {

    val logsList: MutableLiveData<Resource<ReleasesResponse>> = MutableLiveData()
    private var logsListResponse: ReleasesResponse? = null

    init {
        getReleasesLog()
    }

    private fun getReleasesLog() =
        viewModelScope.launch {
            logsList.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = releasesRepository.getReleaseLogs()
                    logsList.postValue(handleLogsListResponse(response))
                } else {
                    logsList.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> logsList.postValue(Resource.Error("Network Failure"))
                    else -> logsList.postValue(
                        Resource.Error(
                            t.message ?: "Something went wrong"
                        )
                    )
                }
            }
        }

    private fun handleLogsListResponse(response: Response<ReleasesResponse>): Resource<ReleasesResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (logsListResponse == null) {
                    logsListResponse = resultResponse
                } else {
                    val oldArticles = logsListResponse?.releasesLog
                    val newArticles = resultResponse.releasesLog
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(logsListResponse ?: resultResponse)
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