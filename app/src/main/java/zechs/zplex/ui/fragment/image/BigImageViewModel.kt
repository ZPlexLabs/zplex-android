package zechs.zplex.ui.fragment.image

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BigImageViewModel : ViewModel() {

    val imageUrl = MutableLiveData<String>()

    fun setImageUrl(imageUri: String) {
        imageUrl.value = imageUri
        println(imageUrl)
    }
}