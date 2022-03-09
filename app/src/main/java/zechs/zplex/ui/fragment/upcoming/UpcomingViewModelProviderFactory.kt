package zechs.zplex.ui.fragment.upcoming

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.TmdbRepository


@Suppress("UNCHECKED_CAST")
class UpcomingViewModelProviderFactory(
    val app: Application,
    private val tmdbRepository: TmdbRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UpcomingViewModel(app, tmdbRepository) as T
    }

}