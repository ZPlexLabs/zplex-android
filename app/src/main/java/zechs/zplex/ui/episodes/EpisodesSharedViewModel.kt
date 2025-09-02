package zechs.zplex.ui.episodes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zechs.zplex.data.model.tmdb.entities.Season
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.ui.episodes.EpisodesFragment.Companion.TAG
import zechs.zplex.utils.ext.ifNullOrEmpty
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.state.ResourceExt.Companion.postError
import javax.inject.Inject

@HiltViewModel
class EpisodesSharedViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    var showName: String = ""
        private set

    private val _seasons = MutableLiveData<Resource<List<Season>>>()
    val seasons: LiveData<Resource<List<Season>>> = _seasons

    private val _selectedSeasonNumber = MutableLiveData<Int>()
    val selectedSeasonNumber: LiveData<Int> = _selectedSeasonNumber

    fun loadSeasons(
        showId: Int,
        showName: String,
        seasons: List<Season>
    ) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "loadSeasons() called with: showId=$showId, showName=$showName, seasonsSize=${seasons.size}")

        _seasons.postValue(Resource.Loading())
        this@EpisodesSharedViewModel.showName = showName

        if (seasons.isNotEmpty()) {
            Log.d(TAG, "Using cached seasons list (size=${seasons.size})")
            _seasons.postValue(Resource.Success(seasons))
            return@launch
        }

        Log.d(TAG, "Fetching seasons from repository for showId=$showId")
        try {
            val response = tmdbRepository.getShow(showId)
            Log.d(TAG, "API Response -> isSuccessful=${response.isSuccessful}, code=${response.code()}, message=${response.message()}")

            if (response.isSuccessful && response.body() != null) {
                val fetchedSeasons = response.body()?.seasons ?: emptyList()
                Log.d(TAG, "Fetched ${fetchedSeasons.size} seasons from API")
                _seasons.postValue(Resource.Success(fetchedSeasons))
            } else {
                val errorMsg = response.message().ifNullOrEmpty { "Something went wrong!" }
                Log.e(TAG, "API call failed -> $errorMsg")
                _seasons.postValue(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching seasons: ${e.localizedMessage}", e)
            _seasons.postValue(postError(e))
        }
    }

    fun selectSeason(seasonNumber: Int) {
        if (_selectedSeasonNumber.value != seasonNumber) {
            _selectedSeasonNumber.value = seasonNumber
        }
    }
}
