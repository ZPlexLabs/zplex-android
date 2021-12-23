package zechs.zplex.ui.fragment.image

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BigImageViewModel : ViewModel() {

    private val _imagePath = MutableLiveData<String?>()
    val imagePath: LiveData<String?> get() = _imagePath

    fun setImagePath(imagePath: String?) {
        if (_imagePath.value == imagePath) return
        _imagePath.value = imagePath
    }

}