package zechs.zplex.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.SeriesRepository

@Suppress("UNCHECKED_CAST")
class SeriesViewModelProviderFactory(
    val app: Application,
    private val seriesRepository: SeriesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SeriesViewModel(app, seriesRepository) as T
    }
}