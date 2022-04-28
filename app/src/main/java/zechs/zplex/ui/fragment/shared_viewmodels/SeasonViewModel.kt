package zechs.zplex.ui.fragment.shared_viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.models.dataclass.ShowSeason


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
