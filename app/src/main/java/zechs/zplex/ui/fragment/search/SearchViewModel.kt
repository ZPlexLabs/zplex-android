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
import zechs.zplex.models.drive.DriveResponse
import zechs.zplex.repository.FilesRepository
import zechs.zplex.utils.Constants.PAGE_TOKEN
import zechs.zplex.utils.Constants.TEMP_TOKEN
import zechs.zplex.utils.Constants.isLastPage
import zechs.zplex.utils.Resource
import zechs.zplex.utils.SessionManager
import java.io.IOException


class SearchViewModel(
    app: Application,
    private val filesRepository: FilesRepository
) : AndroidViewModel(app) {

    val searchList: MutableLiveData<Resource<DriveResponse>> = MutableLiveData()
    private var searchListResponse: DriveResponse? = null

    private var newSearchQuery: String? = null
    private var oldSearchQuery: String? = null

    private val accessToken = SessionManager(
        getApplication<Application>().applicationContext
    ).fetchAuthToken()

    private val pageSize = 21
    private val orderBy = "modifiedTime desc"

    init {
        getSearchList(
            "(name contains 'TV' or name contains 'Movie') and '0AASFDMjRqUB0Uk9PVA' in parents and trashed = false",
            ""
        )
    }

    fun getSearchList(searchQuery: String, pageToken: String) =
        viewModelScope.launch {
            newSearchQuery = searchQuery
            searchList.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = filesRepository.getDriveFiles(
                        pageSize,
                        if (accessToken == "") TEMP_TOKEN else accessToken,
                        pageToken, searchQuery, orderBy
                    )
                    searchList.postValue(handleSearchListResponse(response))
                } else {
                    searchList.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)

                if (t is IOException) {
                    searchList.postValue(Resource.Error("Network Failure"))
                } else {
                    searchList.postValue(
                        Resource.Error(
                            t.message ?: "Something went wrong"
                        )
                    )
                }
            }
        }

    private fun handleSearchListResponse(
        response: Response<DriveResponse>
    ): Resource<DriveResponse> {
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