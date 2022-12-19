package zechs.zplex.ui.upcoming

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.data.model.tmdb.search.SearchResponse
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.state.ResourceExt.Companion.postError
import zechs.zplex.utils.util.Converter
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UpcomingViewModel @Inject constructor(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : BaseAndroidViewModel(app) {

    private val _upcoming = MutableLiveData<Resource<SearchResponse>>()
    val upcoming: LiveData<Resource<SearchResponse>>
        get() = _upcoming

    private var upcomingResponse: SearchResponse? = null
    private var page = 1
    private var count = 0

    init {
        page = 1
        getUpcoming()
    }

    fun getUpcoming() = viewModelScope.launch(Dispatchers.IO) {
        _upcoming.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = tmdbRepository.getUpcoming(page)
                _upcoming.postValue(handleBrowseResponse(response))
            } else {
                _upcoming.postValue(Resource.Error("No internet connection"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _upcoming.postValue(postError(e))
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
                        Converter.dateToLocalDate(
                            it.releasedDate()!!
                        ).isAfter(LocalDate.now())
                    }.sortedBy {
                        Converter.dateToLocalDate(
                            it.releasedDate()!!
                        )
                    }.distinctBy {
                        it.id
                    }.toMutableList()
                )
                if (responseList.results.size < 5 && count <= 5) {
                    count++
                    viewModelScope.launch { getUpcoming() }
                }
                return Resource.Success(responseList)
            }
        }
        return Resource.Error(response.message())
    }


}