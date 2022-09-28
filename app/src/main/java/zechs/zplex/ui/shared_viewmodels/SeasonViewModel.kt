package zechs.zplex.ui.shared_viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

@Keep
data class ShowSeason(
    val tmdbId: Int,
    val seasonName: String?,
    val seasonNumber: Int,
    val showName: String,
    val seasonPosterPath: String?,
    val showPoster: String?
)

class SeasonViewModel : ViewModel() {

    private val _showSeason = MutableLiveData<ShowSeason>()
    val showId: LiveData<ShowSeason> get() = _showSeason

    fun setShowSeason(
        tmdbId: Int,
        seasonName: String?,
        seasonNumber: Int,
        showName: String,
        seasonPosterPath: String?,
        showPoster: String?
    ) {
        val update =
            ShowSeason(tmdbId, seasonName, seasonNumber, showName, seasonPosterPath, showPoster)
        if (_showSeason.value == update) return
        _showSeason.value = update
    }
}
