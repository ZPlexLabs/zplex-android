package zechs.zplex.ui.fragment.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.adapter.list.ListDataModel
import zechs.zplex.models.tmdb.entities.Cast
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.models.tmdb.entities.Season
import zechs.zplex.models.tmdb.entities.Video

class ListViewModel : ViewModel() {

    private val _listArgs = MutableLiveData<ListDataModel>()
    val listArgs: LiveData<ListDataModel> get() = _listArgs

    fun setSeasonsList(
        tmdbId: Int,
        showName: String,
        showPoster: String?,
        seasons: List<Season>
    ) {
        setList(ListDataModel.Seasons(tmdbId, showName, showPoster, seasons))
    }

    fun setCasts(casts: List<Cast>) {
        setList(ListDataModel.Casts(casts = casts))
    }

    fun setMedia(media: List<Media>) {
        setList(ListDataModel.Media(media))
    }

    fun setVideo(video: List<Video>) {
        setList(ListDataModel.Videos(video))
    }

    private fun <T : ListDataModel> setList(list: T) {
        if (_listArgs.value == list) return
        _listArgs.value = list
    }

}