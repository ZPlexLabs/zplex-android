package zechs.zplex.ui

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
import zechs.zplex.models.drive.File
import zechs.zplex.repository.FilesRepository
import zechs.zplex.utils.Constants.Companion.PAGE_TOKEN
import zechs.zplex.utils.Resource
import zechs.zplex.utils.SessionManager
import java.io.IOException


class FileViewModel(
    app: Application,
    private val filesRepository: FilesRepository
) : AndroidViewModel(app) {

    val homeList: MutableLiveData<Resource<DriveResponse>> = MutableLiveData()
    val searchList: MutableLiveData<Resource<DriveResponse>> = MutableLiveData()
    val mediaList: MutableLiveData<Resource<DriveResponse>> = MutableLiveData()

    private var searchListResponse: DriveResponse? = null
    private val tempAccessToken =
        "ya29.a0ARrdaM-Eo6FAlBA4oY9LkSYHNi79ulu8NfovWyvKypPqQ682tICYQU2l7SH-4UfVt2nOveHpsdniCgwXsN8c1ATeCShidcJMgnAdzDtYADCS_heFn0udfMOVxwwfFY2cxzy0CD9Eh68xggCUa2iyVLGRAYvG9w"

    private var newSearchQuery: String? = null
    private var oldSearchQuery: String? = null

    init {
        getHomeList()
        getSearchList(
            "mimeType='application/vnd.google-apps.folder' and '0AASFDMjRqUB0Uk9PVA' in parents and trashed = false",
            ""
        )
    }

    private fun getHomeList() = viewModelScope.launch {
        homeList.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val accessToken =
                    SessionManager(getApplication<Application>().applicationContext).fetchAuthToken()
                val response = if (accessToken == "") {
                    filesRepository.getDriveFiles(
                        20,
                        tempAccessToken,
                        "",
                        "mimeType='application/vnd.google-apps.folder' and '0AASFDMjRqUB0Uk9PVA' in parents and trashed = false",
                        "modifiedTime desc"
                    )
                } else {
                    filesRepository.getDriveFiles(
                        20,
                        accessToken,
                        "",
                        "mimeType='application/vnd.google-apps.folder' and '0AASFDMjRqUB0Uk9PVA' in parents and trashed = false",
                        "modifiedTime desc"
                    )
                }
                homeList.postValue(handleHomeListResponse(response))
            } else {
                homeList.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            println(t.stackTrace)
            println(t.message)
            when (t) {
                is IOException -> homeList.postValue(Resource.Error("Network Failure"))
                else -> homeList.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun handleHomeListResponse(response: Response<DriveResponse>): Resource<DriveResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun getSearchList(searchQuery: String, pageToken: String) =
        viewModelScope.launch {
            newSearchQuery = searchQuery
            searchList.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val accessToken =
                        SessionManager(getApplication<Application>().applicationContext).fetchAuthToken()
                    val response = if (accessToken == "") {
                        filesRepository.getDriveFiles(
                            20,
                            tempAccessToken,
                            pageToken,
                            searchQuery,
                            "name desc"
                        )
                    } else {
                        filesRepository.getDriveFiles(
                            20,
                            accessToken,
                            pageToken,
                            searchQuery,
                            "name desc"
                        )
                    }
                    searchList.postValue(handleSearchListResponse(response))
                } else {
                    searchList.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)
                when (t) {
                    is IOException -> searchList.postValue(Resource.Error("Network Failure"))
                    else -> searchList.postValue(Resource.Error("Conversion Error"))
                }
            }
        }

    private fun handleSearchListResponse(response: Response<DriveResponse>): Resource<DriveResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (searchListResponse == null || newSearchQuery != oldSearchQuery) {
                    PAGE_TOKEN = ""
                    oldSearchQuery = newSearchQuery
                    searchListResponse = resultResponse
                } else {
                    PAGE_TOKEN = resultResponse.nextPageToken ?: PAGE_TOKEN
                    val oldItems = searchListResponse?.files
                    val newItems = resultResponse.files
                    oldItems?.addAll(newItems)
                }
                return Resource.Success(searchListResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


    fun saveFile(file: File) = viewModelScope.launch {
        filesRepository.upsert(file)
    }

    fun getSavedFiles() = filesRepository.getSavedFiles()

    fun deleteFile(file: File) = viewModelScope.launch {
        filesRepository.deleteFile(file)
    }

    fun getMediaFiles(driveQuery: String) =
        viewModelScope.launch {
            mediaList.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val accessToken =
                        SessionManager(getApplication<Application>().applicationContext).fetchAuthToken()
                    val response = if (accessToken == "") {
                        filesRepository.getDriveFiles(
                            1000, tempAccessToken, "", driveQuery, "name"
                        )
                    } else {
                        filesRepository.getDriveFiles(
                            1000, accessToken, "", driveQuery, "name"
                        )
                    }
                    mediaList.postValue(handleMediaListResponse(response))
                } else {
                    mediaList.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> mediaList.postValue(Resource.Error("Network Failure"))
                    else -> mediaList.postValue(Resource.Error("Conversion Error"))
                }
            }
        }

    private fun handleMediaListResponse(response: Response<DriveResponse>): Resource<DriveResponse> {
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