package zechs.zplex.ui.viewmodel.tvdb

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.TvdbRepository

@Suppress("UNCHECKED_CAST")
class TvdbViewModelProviderFactory(
    val app: Application,
    private val tvdbRepository: TvdbRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TvdbViewModel(app, tvdbRepository) as T
    }
}