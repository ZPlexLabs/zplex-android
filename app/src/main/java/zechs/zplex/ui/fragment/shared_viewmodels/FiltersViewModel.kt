package zechs.zplex.ui.fragment.shared_viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import zechs.zplex.models.dataclass.FilterArgs
import zechs.zplex.models.enum.MediaType
import zechs.zplex.models.enum.Order
import zechs.zplex.models.enum.SortBy
import zechs.zplex.models.tmdb.keyword.TmdbKeyword


class FiltersViewModel : ViewModel() {

    private val _filterArgs = MutableLiveData<FilterArgs>()
    val filterArgs: LiveData<FilterArgs> get() = _filterArgs

    init {
        setFilter(
            mediaType = MediaType.movie,
            sortBy = SortBy.popularity,
            order = Order.desc,
            page = 1,
            withKeyword = null,
            withGenres = null
        )
    }

    fun setFilter(
        mediaType: MediaType,
        sortBy: SortBy,
        order: Order,
        page: Int,
        withKeyword: List<TmdbKeyword>?,
        withGenres: Int?
    ) {
        val update = FilterArgs(mediaType, sortBy, order, page, withKeyword, withGenres)
        if (_filterArgs.value == update) return
        _filterArgs.value = update
    }

    fun getFilter() = _filterArgs.value
}
