package zechs.zplex.ui.fragment.myshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.FilesRepository

@Suppress("UNCHECKED_CAST")
class MyShowsViewModelProviderFactory(
    private val filesRepository: FilesRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MyShowsViewModel(filesRepository) as T
    }

}