package zechs.zplex.ui.fragment.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.models.dataclass.CastArgs

class CastDetailsViewModel : ViewModel() {

    private val _castArgs = MutableLiveData<CastArgs>()
    val castArgs: LiveData<CastArgs> get() = _castArgs

    fun setCast(personId: Int, creditId: String) {
        val update = CastArgs(creditId, personId)
        if (_castArgs.value == update) return
        _castArgs.value = update
    }
}
