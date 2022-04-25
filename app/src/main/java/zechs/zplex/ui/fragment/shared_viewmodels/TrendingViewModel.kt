package zechs.zplex.ui.fragment.shared_viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.models.tmdb.entities.Media

class TrendingViewModel : ViewModel() {

    private val _trendingBanner = MutableLiveData<Media>()
    val trendingBanner: LiveData<Media> get() = _trendingBanner

    fun setTrendingBanner(media: Media) {
        if (_trendingBanner.value == media) return
        _trendingBanner.value = media
    }

}
