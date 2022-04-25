package zechs.zplex.ui.fragment.episodes

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.repository.ZPlexRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.ui.seasonResponseZplex
import zechs.zplex.utils.Event
import zechs.zplex.utils.Resource
import java.io.IOException

class EpisodesViewModel(
    app: Application,
    private val zplexRepository: ZPlexRepository
) : BaseAndroidViewModel(app) {

    private val _seasonZplex = MutableLiveData<Event<Resource<seasonResponseZplex>>>()
    val seasonZplex: LiveData<Event<Resource<seasonResponseZplex>>>
        get() = _seasonZplex

    fun zplexGetSeason(tvId: Int, seasonNumber: Int) = viewModelScope.launch {
        _seasonZplex.postValue(Event(Resource.Loading()))
        try {
            if (hasInternetConnection()) {
                val zplexMovie = zplexRepository.getSeason(tvId, seasonNumber)
                _seasonZplex.postValue(Event(handleZplexSeasonResponse(zplexMovie)))
            } else {
                _seasonZplex.postValue(Event(Resource.Error("No internet connection")))
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            println("zplexGetSeason :  Message=${t.message}")
            _seasonZplex.postValue(
                Event(
                    Resource.Error(
                        if (t is IOException) {
                            "Network Failure"
                        } else t.message ?: "Something went wrong"
                    )
                )
            )
        }
    }

    private fun handleZplexSeasonResponse(
        response: Response<seasonResponseZplex>
    ): Resource<seasonResponseZplex> {
        if (response.isSuccessful) {
            return Resource.Success(response.body()!!)
        }
        return Resource.Error(response.message())
    }
}