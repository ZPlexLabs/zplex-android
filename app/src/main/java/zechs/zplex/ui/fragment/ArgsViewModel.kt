package zechs.zplex.ui.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.models.Args

class ArgsViewModel : ViewModel() {

    val args = MutableLiveData<Args>()

    fun setArg(arg: Args) {
        args.value = arg
        println(arg)
    }
}