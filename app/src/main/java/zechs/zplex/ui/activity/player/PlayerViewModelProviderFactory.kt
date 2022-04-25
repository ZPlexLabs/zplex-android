package zechs.zplex.ui.activity.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import zechs.zplex.repository.WatchedRepository


@Suppress("UNCHECKED_CAST")
class PlayerViewModelProviderFactory(
    private val watchedRepository: WatchedRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlayerViewModel(watchedRepository) as T
    }

}