package zechs.zplex.ui.fragment.episodes

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.repository.ZPlexRepository

@Suppress("UNCHECKED_CAST")
class EpisodesViewModelProviderFactory(
    val app: Application,
    private val tmdbRepository: TmdbRepository,
    private val zplexRepository: ZPlexRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EpisodesViewModel(app, tmdbRepository, zplexRepository) as T
    }

}