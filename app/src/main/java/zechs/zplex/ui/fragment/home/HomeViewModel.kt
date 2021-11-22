package zechs.zplex.ui.fragment.home

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
import zechs.zplex.models.drive.DriveResponse
import zechs.zplex.models.witch.ReleasesResponse
import zechs.zplex.repository.FilesRepository
import zechs.zplex.repository.ReleasesRepository
import zechs.zplex.utils.Constants.TEMP_TOKEN
import zechs.zplex.utils.Resource
import zechs.zplex.utils.SessionManager
import java.io.IOException


class HomeViewModel(
    app: Application,
    private val filesRepository: FilesRepository,
    private val releasesRepository: ReleasesRepository
) : AndroidViewModel(app) {

    val homeList: MutableLiveData<Resource<DriveResponse>> = MutableLiveData()
    val logsList: MutableLiveData<Resource<ReleasesResponse>> = MutableLiveData()

    private val accessToken =
        SessionManager(getApplication<Application>().applicationContext).fetchAuthToken()
    private val driveQuery =
        "(name contains 'TV' or name contains 'Movie') and '0AASFDMjRqUB0Uk9PVA' in parents and trashed = false"
    private val pageSize = 10
    private val orderBy = "modifiedTime desc"

    init {
        getHomeList()
        getReleasesLog()
    }

    fun getSavedShows() = filesRepository.getSavedFiles()

    private fun getHomeList() = viewModelScope.launch {
        homeList.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = filesRepository.getDriveFiles(
                    pageSize,
                    if (accessToken == "") TEMP_TOKEN else accessToken,
                    "", driveQuery, orderBy
                )
                homeList.postValue(handleHomeListResponse(response))
            } else {
                homeList.postValue(Resource.Error("No internet connection"))
            }

        } catch (t: Throwable) {
            println(t.stackTrace)
            println(t.message)

            if (t is IOException) {
                homeList.postValue(Resource.Error("Network Failure"))
            } else {
                homeList.postValue(
                    Resource.Error(
                        t.message ?: "Something went wrong"
                    )
                )
            }
        }
    }

    private fun handleHomeListResponse(
        response: Response<DriveResponse>
    ): Resource<DriveResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
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
                println(t.stackTrace)
                println(t.message)

                if (t is IOException) {
                    logsList.postValue(Resource.Error("Network Failure"))
                } else {
                    logsList.postValue(
                        Resource.Error(
                            t.message ?: "Something went wrong"
                        )
                    )
                }
            }
        }

    private fun handleLogsListResponse(
        response: Response<ReleasesResponse>
    ): Resource<ReleasesResponse> {
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