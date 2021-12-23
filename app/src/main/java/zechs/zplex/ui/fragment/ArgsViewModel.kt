package zechs.zplex.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.models.Args

class ArgsViewModel : ViewModel() {

    private val _args = MutableLiveData<Args>()
    val args: LiveData<Args> get() = _args

    fun setArg(arg: Args) {
        if (_args.value == arg) return
        _args.value = arg
    }
}
