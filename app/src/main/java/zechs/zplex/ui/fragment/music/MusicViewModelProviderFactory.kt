package zechs.zplex.ui.fragment.music

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.FilesRepository

@Suppress("UNCHECKED_CAST")
class MusicViewModelProviderFactory(
    val app: Application,
    private val filesRepository: FilesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MusicViewModel(app, filesRepository) as T
    }

}