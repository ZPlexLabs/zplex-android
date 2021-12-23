package zechs.zplex.ui.fragment.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.models.dataclass.FilterArgs


class FiltersViewModel : ViewModel() {

    private val _filterArgs = MutableLiveData<FilterArgs>()
    val filterArgs: LiveData<FilterArgs> get() = _filterArgs

    init {
        setFilter(
            mediaType = "tv",
            sortBy = "popularity.desc",
            page = 1,
            withKeyword = 0,
            withGenres = 0
        )
    }

    fun setFilter(
        mediaType: String,
        sortBy: String,
        page: Int,
        withKeyword: Int?,
        withGenres: Int
    ) {
        val update = FilterArgs(mediaType, sortBy, page, withKeyword, withGenres)
        if (_filterArgs.value == update) return
        _filterArgs.value = update
    }


// TODO: Try out Shared flow
//
//    private val _filterArgs = MutableSharedFlow<FilterArgs>()
//    val filterArgs: SharedFlow<FilterArgs> = _filterArgs
//
//    fun setFilter(
//        mediaType: String,
//        sortBy: String,
//        page: Int,
//        withKeyword: String,
//        withGenres: String
//    ) {
//        val update = FilterArgs(mediaType, sortBy, page, withKeyword, withGenres)
//        if (_filterArgs. == update) return
//        _filterArgs.value = update
//    }
}
