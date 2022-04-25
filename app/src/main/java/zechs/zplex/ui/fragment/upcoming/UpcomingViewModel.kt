package zechs.zplex.ui.fragment.upcoming

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.models.tmdb.search.SearchResponse
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.utils.Resource
import java.io.IOException

class UpcomingViewModel(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : BaseAndroidViewModel(app) {

    val upcoming: MutableLiveData<Resource<SearchResponse>> = MutableLiveData()
    private var upcomingResponse: SearchResponse? = null
    private var page = 1

    init {
        page = 1
        getUpcoming()
    }

    fun getUpcoming() = viewModelScope.launch {
        println("page=$page")
        upcoming.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = tmdbRepository.getUpcoming(page)
                upcoming.postValue(handleBrowseResponse(response))
            } else {
                upcoming.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            println(t.stackTrace)
            println(t.message)
            upcoming.postValue(
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
                if (upcomingResponse == null) {
                    page = 2
                    upcomingResponse = resultResponse
                } else {
                    val oldItems = upcomingResponse?.results
                    val newItems = resultResponse.results
                    oldItems?.addAll(newItems)
                }
                return Resource.Success(upcomingResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


}