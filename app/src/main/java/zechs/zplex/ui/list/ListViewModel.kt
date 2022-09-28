package zechs.zplex.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.data.model.tmdb.entities.Cast
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.data.model.tmdb.entities.Season
import zechs.zplex.data.model.tmdb.entities.Video
import zechs.zplex.ui.list.adapter.ListDataModel
import zechs.zplex.utils.state.Event

class ListViewModel : ViewModel() {

    private val _listArgs = MutableLiveData<Event<ListDataModel>>()
    val listArgs: LiveData<Event<ListDataModel>> get() = _listArgs

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

    fun setMedia(heading: String, media: List<Media>) {
        setList(ListDataModel.Media(heading, media))
    }

    fun setVideo(video: List<Video>) {
        setList(ListDataModel.Videos(video))
    }

    private fun <T : ListDataModel> setList(list: T) {
        _listArgs.value = Event(list)
    }

}