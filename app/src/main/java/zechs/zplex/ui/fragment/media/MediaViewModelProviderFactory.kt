package zechs.zplex.ui.fragment.media

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.repository.ZPlexRepository


@Suppress("UNCHECKED_CAST")
class MediaViewModelProviderFactory(
    val app: Application,
    private val tmdbRepository: TmdbRepository,
    private val zplexRepository: ZPlexRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MediaViewModel(app, tmdbRepository, zplexRepository) as T
    }

}