package zechs.zplex.ui.fragment.episodes

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.adapter.episodes.EpisodesDataModel
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.ui.seasonResponseTmdb
import zechs.zplex.utils.Event
import zechs.zplex.utils.Resource
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : BaseAndroidViewModel(app) {

    private val _seasonResponse = MutableLiveData<Event<Resource<List<EpisodesDataModel>>>>()
    val episodesResponse: LiveData<Event<Resource<List<EpisodesDataModel>>>>
        get() = _seasonResponse

    fun getSeason(
        tmdbId: Int,
        seasonNumber: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        _seasonResponse.postValue(Event(Resource.Loading()))
        try {
            if (hasInternetConnection()) {
                val tmdbSeason = tmdbRepository.getSeason(tmdbId, seasonNumber)
                _seasonResponse.postValue(Event(handleSeasonResponse(tmdbSeason)))
            } else {
                _seasonResponse.postValue(Event(Resource.Error("No internet connection")))
            }
        } catch (e: Exception) {
            e.printStackTrace()

            val errorMsg = if (e is IOException) {
                "Network Failure"
            } else e.message ?: "Something went wrong"

            _seasonResponse.postValue(Event(Resource.Error(errorMsg)))
        }
    }

    private fun handleSeasonResponse(
        response: Response<seasonResponseTmdb>
    ): Resource<List<EpisodesDataModel>> {
        if (response.body() != null) {
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

            val episodesList = result.episodes?.map {
                it.toZplex()
            }
            if (episodesList != null && episodesList.isNotEmpty()) {
                seasonDataModel.add(
                    EpisodesDataModel.Episodes(
                        episodes = episodesList,
                        accessToken = null
                    )
                )
            }

            return Resource.Success(seasonDataModel.toList())
        }
        return Resource.Error(response.message())
    }
}