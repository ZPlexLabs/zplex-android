package zechs.zplex.ui.fragment.myshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.TmdbRepository

@Suppress("UNCHECKED_CAST")
class MyShowsViewModelProviderFactory(
    private val tmdbRepository: TmdbRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MyShowsViewModel(tmdbRepository) as T
    }

}