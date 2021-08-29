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
import zechs.zplex.api.SessionManager
import zechs.zplex.models.drive.DriveResponse
import zechs.zplex.models.drive.File
import zechs.zplex.repository.FilesRepository
import zechs.zplex.utils.Constants.Companion.PAGE_TOKEN
import zechs.zplex.utils.Resource
import java.io.IOException


class FileViewModel(
    app: Application,
    private val filesRepository: FilesRepository
) : AndroidViewModel(app) {

    val filesList: MutableLiveData<Resource<DriveResponse>> = MutableLiveData()
    private var filesListResponse: DriveResponse? = null
    private val tempAccessToken =
        "ya29.a0ARrdaM-Eo6FAlBA4oY9LkSYHNi79ulu8NfovWyvKypPqQ682tICYQU2l7SH-4UfVt2nOveHpsdniCgwXsN8c1ATeCShidcJMgnAdzDtYADCS_heFn0udfMOVxwwfFY2cxzy0CD9Eh68xggCUa2iyVLGRAYvG9w"

    init {
        getDriveFiles(
            15,
            PAGE_TOKEN,
            "mimeType='application/vnd.google-apps.folder' and parents in '0AASFDMjRqUB0Uk9PVA' and trashed = false"
        )
    }

    fun getDriveFiles(pageSize: Int, pageToken: String, driveQuery: String) =
        viewModelScope.launch {
            filesList.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val accessToken =
                        SessionManager(getApplication<Application>().applicationContext).fetchAuthToken()
                    val response = if (accessToken == "") {
                        filesRepository.getDriveFiles(
                            pageSize,
                            tempAccessToken,
                            pageToken,
                            driveQuery
                        )
                    } else {
                        filesRepository.getDriveFiles(pageSize, accessToken, pageToken, driveQuery)
                    }

                    filesList.postValue(handleFilesListResponse(response, driveQuery))
                } else {
                    filesList.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> filesList.postValue(Resource.Error("Network Failure"))
                    else -> filesList.postValue(Resource.Error("Conversion Error"))
                }
            }
        }

    private fun handleFilesListResponse(
        response: Response<DriveResponse>,
        driveQuery: String
    ): Resource<DriveResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                PAGE_TOKEN = resultResponse.nextPageToken ?: PAGE_TOKEN
                if (filesListResponse == null || driveQuery != "") {
                    filesListResponse = resultResponse
                } else {
                    val oldArticles = filesListResponse?.files
                    val newArticles = resultResponse.files
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(filesListResponse ?: resultResponse)
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