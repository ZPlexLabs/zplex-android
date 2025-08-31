package zechs.zplex.ui.shared_viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.data.model.tmdb.entities.Season

@Keep
data class ShowSeason(
    val tmdbId: Int,
    val seasonName: String?,
    val seasonNumber: Int,
    val showName: String,
    val seasonPosterPath: String?,
    val showPoster: String?,
    val seasons: List<Season>
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
        showPoster: String?,
        seasons: List<Season>
    ) {
        val update = ShowSeason(
            tmdbId,
            seasonName,
            seasonNumber,
            showName,
            seasonPosterPath,
            showPoster,
            seasons
        )
        if (_showSeason.value == update) return
        _showSeason.value = update
    }
}
