package zechs.zplex.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.models.MediaArgs

class ShowViewModel : ViewModel() {

    private val _mediaArgs = MutableLiveData<MediaArgs>()
    val mediaArgs: LiveData<MediaArgs> get() = _mediaArgs

    fun setMedia(id: Int, mediaType: String) {
        val update = MediaArgs(id, mediaType)
        if (_mediaArgs.value == update) return
        _mediaArgs.value = update
    }
}
