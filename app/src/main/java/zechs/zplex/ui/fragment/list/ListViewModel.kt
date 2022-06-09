package zechs.zplex.ui.fragment.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.adapter.list.ListDataModel
import zechs.zplex.models.tmdb.entities.Cast
import zechs.zplex.models.tmdb.entities.Season

class ListViewModel : ViewModel() {

    private val _listArgs = MutableLiveData<ListDataModel>()
    val listArgs: LiveData<ListDataModel> get() = _listArgs

    fun setSeasonsList(
        tmdbId: Int,
        showName: String,
        showPoster: String?,
        seasons: List<Season>
    ) {
        val update = ListDataModel.Seasons(tmdbId, showName, showPoster, seasons)
        if (_listArgs.value == update) return
        _listArgs.value = update
    }


    fun setCasts(casts: List<Cast>) {
        val update = ListDataModel.Casts(casts = casts)
        if (_listArgs.value == update) return
        _listArgs.value = update
    }

}
