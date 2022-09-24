package zechs.zplex.ui.fragment.upcoming

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.models.tmdb.search.SearchResponse
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.utils.ConverterUtils
import zechs.zplex.utils.Resource
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UpcomingViewModel @Inject constructor(
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
        upcoming.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = tmdbRepository.getUpcoming(page)
                upcoming.postValue(handleBrowseResponse(response))
            } else {
                upcoming.postValue(Resource.Error("No internet connection"))
            }
        } catch (e: Exception) {
            e.printStackTrace()

            upcoming.postValue(
                Resource.Error(
                    if (e is IOException) {
                        "Network Failure"
                    } else e.message ?: "Something went wrong"
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
                var responseList = upcomingResponse ?: resultResponse
                responseList = responseList.copy(
                    results = responseList.results.asSequence().filter {
                        it.releasedDate() != null
                    }.filter {
                        ConverterUtils.dateToLocalDate(
                            it.releasedDate()!!
                        ).isAfter(LocalDate.now())
                    }.sortedBy {
                        ConverterUtils.dateToLocalDate(
                            it.releasedDate()!!
                        )
                    }.distinctBy {
                        it.id
                    }.toMutableList()
                )
                return Resource.Success(responseList)
            }
        }
        return Resource.Error(response.message())
    }


}