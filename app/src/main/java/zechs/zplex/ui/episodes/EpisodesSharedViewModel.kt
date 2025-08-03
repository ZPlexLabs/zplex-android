package zechs.zplex.ui.episodes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zechs.zplex.data.model.tmdb.entities.Season
import zechs.zplex.data.repository.TmdbRepository
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

    fun loadSeasons(showId: Int, showName: String) = viewModelScope.launch(Dispatchers.IO) {
        _seasons.postValue(Resource.Loading())
        this@EpisodesSharedViewModel.showName = showName
        try {
            val seasonsList = tmdbRepository.getShow(showId)
            if (seasonsList.isSuccessful && seasonsList.body() != null) {
                _seasons.postValue(Resource.Success(seasonsList.body()!!.seasons ?: listOf()))
            } else {
                _seasons.postValue(
                    Resource.Error(
                        seasonsList.message().toString().ifNullOrEmpty { "Something went wrong!" })
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _seasons.postValue(postError(e))
        }
    }

    fun selectSeason(seasonNumber: Int) {
        if (_selectedSeasonNumber.value != seasonNumber) {
            _selectedSeasonNumber.value = seasonNumber
        }
    }
}
