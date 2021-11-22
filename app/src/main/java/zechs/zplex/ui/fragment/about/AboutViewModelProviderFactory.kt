package zechs.zplex.ui.fragment.about

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.FilesRepository
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.repository.TvdbRepository

@Suppress("UNCHECKED_CAST")
class AboutViewModelProviderFactory(
    val app: Application,
    private val filesRepository: FilesRepository,
    private val tvdbRepository: TvdbRepository,
    private val tmdbRepository: TmdbRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AboutViewModel(
            app,
            filesRepository,
            tvdbRepository,
            tmdbRepository
        ) as T
    }

}