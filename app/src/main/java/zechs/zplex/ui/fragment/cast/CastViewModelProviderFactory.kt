package zechs.zplex.ui.fragment.cast

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.TmdbRepository


@Suppress("UNCHECKED_CAST")
class CastViewModelProviderFactory(
    val app: Application,
    private val tmdbRepository: TmdbRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CastViewModel(app, tmdbRepository) as T
    }

}