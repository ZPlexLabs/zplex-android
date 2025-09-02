package zechs.zplex.ui.shared_viewmodels

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import zechs.zplex.data.model.tmdb.entities.Season
import javax.inject.Inject

@Keep
@Parcelize
data class ShowSeason(
    val tmdbId: Int,
    val seasonName: String?,
    val seasonNumber: Int,
    val showName: String,
    val seasonPosterPath: String?,
    val showPoster: String?,
    val seasons: List<Season>
) : Parcelable

@HiltViewModel
class SeasonViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_SHOW_SEASON = "show_season"
    }

    private val _showSeason: MutableLiveData<ShowSeason> = savedStateHandle.getLiveData(KEY_SHOW_SEASON)
    val showId: LiveData<ShowSeason> get() = _showSeason

    fun setShowSeason(
        tmdbId: Int,
        seasonName: String?,
        seasonNumber: Int,
        showName: String,
        seasonPosterPath: String?,
        showPoster: String?,
        seasons: List<Season>
    ) {
        val update = ShowSeason(tmdbId, seasonName, seasonNumber, showName, seasonPosterPath, showPoster, seasons)
        if (_showSeason.value == update) return
        _showSeason.value = update
        savedStateHandle[KEY_SHOW_SEASON] = update
    }
}
