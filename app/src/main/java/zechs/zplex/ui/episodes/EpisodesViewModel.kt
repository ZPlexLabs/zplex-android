package zechs.zplex.ui.episodes

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.ui.episodes.adapter.EpisodesDataModel
import zechs.zplex.utils.SessionManager
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.state.ResourceExt.Companion.postError
import javax.inject.Inject

typealias seasonResponseTmdb = zechs.zplex.data.model.tmdb.season.SeasonResponse

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    app: Application,
    private val tmdbRepository: TmdbRepository,
    private val sessionManager: SessionManager
) : BaseAndroidViewModel(app) {

    var hasLoggedIn = false
        private set

    fun updateStatus() = viewModelScope.launch {
        hasLoggedIn = getLoginStatus()
    }

    private suspend fun getLoginStatus(): Boolean {
        sessionManager.fetchClient() ?: return false
        sessionManager.fetchRefreshToken() ?: return false
        return true
    }


    private val _seasonResponse = MutableLiveData<Resource<List<EpisodesDataModel>>>()
    val episodesResponse: LiveData<Resource<List<EpisodesDataModel>>>
        get() = _seasonResponse

    fun getSeason(
        tmdbId: Int,
        seasonNumber: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        _seasonResponse.postValue((Resource.Loading()))
        try {
            if (hasInternetConnection()) {
                val tmdbSeason = tmdbRepository.getSeason(tmdbId, seasonNumber)
                _seasonResponse.postValue((handleSeasonResponse(tmdbSeason)))
            } else {
                _seasonResponse.postValue((Resource.Error("No internet connection")))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _seasonResponse.postValue(postError(e))
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

            result.episodes?.forEach {
                seasonDataModel.add(
                    EpisodesDataModel.Episode(
                        id = it.id,
                        name = it.name ?: "TBA",
                        overview = it.name,
                        episode_number = it.episode_number,
                        season_number = it.season_number,
                        still_path = it.still_path,
                        // TODO: Need to change this
                        fileId = null
                    )
                )
            }

            return Resource.Success(seasonDataModel.toList())
        }
        return Resource.Error(response.message())
    }
}