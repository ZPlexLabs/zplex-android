package zechs.zplex.ui.fragment.episodes

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.adapter.episodes.EpisodesDataModel
import zechs.zplex.models.zplex.SeasonResponse
import zechs.zplex.repository.ZPlexRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.utils.Event
import zechs.zplex.utils.Resource
import java.io.IOException

class EpisodesViewModel(
    app: Application,
    private val zplexRepository: ZPlexRepository
) : BaseAndroidViewModel(app) {

    private val _seasonResponse = MutableLiveData<Event<Resource<List<EpisodesDataModel>>>>()
    val episodesResponse: LiveData<Event<Resource<List<EpisodesDataModel>>>>
        get() = _seasonResponse

    fun getSeason(tmdbId: Int, seasonNumber: Int) = viewModelScope.launch {
        _seasonResponse.postValue(Event(Resource.Loading()))
        try {
            if (hasInternetConnection()) {
                val season = zplexRepository.getSeason(tmdbId, seasonNumber)
                _seasonResponse.postValue(Event(handleSeasonResponse(season)))
            } else {
                _seasonResponse.postValue(Event(Resource.Error("No internet connection")))
            }
        } catch (t: Throwable) {
            t.printStackTrace()

            val errorMsg = if (t is IOException) {
                "Network Failure"
            } else t.message ?: "Something went wrong"

            _seasonResponse.postValue(Event(Resource.Error(errorMsg)))
        }
    }

    private fun handleSeasonResponse(
        response: Response<SeasonResponse>
    ): Resource<List<EpisodesDataModel>> {

        if (response.isSuccessful && response.body() != null) {
            val result = response.body()!!
            val seasonDataModel = mutableListOf<EpisodesDataModel>()

            seasonDataModel.add(
                EpisodesDataModel.Header(
                    seasonNumber = "Season ${result.season_number}",
                    seasonName = result.name,
                    seasonPosterPath = result.poster_path,
                    seasonOverview = result.overview ?: "No description"
                )
            )

            val epsiodesList = result.episodes
            if (epsiodesList != null && epsiodesList.isNotEmpty()) {
                seasonDataModel.add(
                    EpisodesDataModel.Episodes(
                        episodes = epsiodesList,
                        accessToken = result.accessToken
                    )
                )
            }

            return Resource.Success(seasonDataModel.toList())
        }
        return Resource.Error(response.message())
    }
}