package zechs.zplex.ui.viewmodel.file

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.util.Log
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
import zechs.zplex.utils.Constants.Companion.isLastPage
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
            "(name contains 'TV' or name contains 'Movie') and '0AASFDMjRqUB0Uk9PVA' in parents and trashed = false",
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
                        10,
                        tempAccessToken,
                        "",
                        "(name contains 'TV' or name contains 'Movie') and '0AASFDMjRqUB0Uk9PVA' in parents and trashed = false",
                        "modifiedTime desc"
                    )
                } else {
                    filesRepository.getDriveFiles(
                        10,
                        accessToken,
                        "",
                        "(name contains 'TV' or name contains 'Movie') and '0AASFDMjRqUB0Uk9PVA' in parents and trashed = false",
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
                else -> homeList.postValue(
                    Resource.Error(
                        t.message ?: "Something went wrong"
                    )
                )
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
                            18,
                            tempAccessToken,
                            pageToken,
                            searchQuery,
                            "modifiedTime desc"
                        )
                    } else {
                        filesRepository.getDriveFiles(
                            18,
                            accessToken,
                            pageToken,
                            searchQuery,
                            "modifiedTime desc"
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
                    else -> searchList.postValue(
                        Resource.Error(
                            t.message ?: "Something went wrong"
                        )
                    )
                }
            }
        }

    private fun handleSearchListResponse(response: Response<DriveResponse>): Resource<DriveResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                PAGE_TOKEN = resultResponse.nextPageToken ?: PAGE_TOKEN
                isLastPage = resultResponse.nextPageToken == null
                if (searchListResponse == null || newSearchQuery != oldSearchQuery) {
                    oldSearchQuery = newSearchQuery
                    searchListResponse = resultResponse
                } else {
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

    fun getFile(id: String) = filesRepository.getFile(id)

    fun getMediaFiles(driveQuery: String) =
        viewModelScope.launch {
            Log.d("ViewModelGetMediaFiles", "executed")
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
                    else -> mediaList.postValue(
                        Resource.Error(
                            t.message ?: "Something went wrong"
                        )
                    )
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