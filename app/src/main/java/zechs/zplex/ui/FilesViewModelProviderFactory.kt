package zechs.zplex.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.FilesRepository

@Suppress("UNCHECKED_CAST")
class FilesViewModelProviderFactory(
    val app: Application,
    private val filesRepository: FilesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FileViewModel(app, filesRepository) as T
    }
}