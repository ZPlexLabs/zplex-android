package zechs.zplex.ui.fragment.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.repository.WatchedRepository

@Suppress("UNCHECKED_CAST")
class HomeViewModelProviderFactory(
    val app: Application,
    private val tmdbRepository: TmdbRepository,
    private val watchedRepository: WatchedRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(app, tmdbRepository, watchedRepository) as T
    }

}
