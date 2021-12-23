package zechs.zplex.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.models.ShowSeason


class SeasonViewModel : ViewModel() {

    private val _showSeason = MutableLiveData<ShowSeason>()
    val showId: LiveData<ShowSeason> get() = _showSeason

    fun setShowSeason(
        driveId: String,
        tmdbId: Int,
        seasonName: String,
        seasonNumber: Int,
        showName: String
    ) {
        val update = ShowSeason(driveId, tmdbId, seasonName, seasonNumber, showName)
        if (_showSeason.value == update) return
        _showSeason.value = update
    }
}
