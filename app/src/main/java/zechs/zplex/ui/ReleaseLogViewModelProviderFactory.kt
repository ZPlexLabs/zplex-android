package zechs.zplex.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.ReleasesRepository


@Suppress("UNCHECKED_CAST")
class ReleaseLogViewModelProviderFactory(
    val app: Application,
    private val releasesRepository: ReleasesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ReleaseLogViewModel(app, releasesRepository) as T
    }
}