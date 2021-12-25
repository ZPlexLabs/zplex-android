package zechs.zplex.ui.fragment.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.models.dataclass.MediaArgs
import zechs.zplex.models.tmdb.entities.Media

class ShowViewModel : ViewModel() {

    private val _mediaArgs = MutableLiveData<MediaArgs>()
    val mediaArgs: LiveData<MediaArgs> get() = _mediaArgs

    fun setMedia(id: Int, mediaType: String, media: Media) {
        val update = MediaArgs(id, mediaType, media)
        if (_mediaArgs.value == update) return
        _mediaArgs.value = update
    }
}
