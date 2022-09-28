package zechs.zplex.ui.shared_viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

@Keep
data class ShowEpisode(
    val tmdbId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int
)

class EpisodeViewModel : ViewModel() {

    private val _showEpisode = MutableLiveData<ShowEpisode>()
    val showEpisode: LiveData<ShowEpisode> get() = _showEpisode

    fun setShowEpisode(
        tmdbId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ) {
        val update = ShowEpisode(tmdbId, seasonNumber, episodeNumber)
        if (_showEpisode.value == update) return
        _showEpisode.value = update
    }
}
