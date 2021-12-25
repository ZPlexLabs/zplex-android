package zechs.zplex.ui.fragment.myshows


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.repository.TmdbRepository


class MyShowsViewModel(
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    fun deleteShow(media: Media) = viewModelScope.launch {
        tmdbRepository.deleteMedia(media)
    }

    fun getSavedMedia() = tmdbRepository.getSavedMedia()

}