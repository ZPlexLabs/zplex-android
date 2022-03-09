package zechs.zplex.ui.fragment.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.models.dataclass.ListArgs
import zechs.zplex.models.tmdb.entities.Cast
import zechs.zplex.models.tmdb.entities.Season

class ListViewModel : ViewModel() {

    private val _listArgs = MutableLiveData<ListArgs>()
    val listArgs: LiveData<ListArgs> get() = _listArgs

    fun setListArgs(tmdbId: Int, showName: String, casts: List<Cast>?, seasons: List<Season>?) {
        val update = ListArgs(tmdbId, showName, casts, seasons)
        if (_listArgs.value == update) return
        _listArgs.value = update
    }
}
