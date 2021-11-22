package zechs.zplex.ui.fragment.myshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import zechs.zplex.models.drive.File
import zechs.zplex.repository.FilesRepository


class MyShowsViewModel(
    private val filesRepository: FilesRepository
) : ViewModel() {

    fun saveShow(file: File) = viewModelScope.launch {
        filesRepository.upsert(file)
    }

    fun deleteShow(file: File) = viewModelScope.launch {
        filesRepository.deleteFile(file)
    }

    fun getSavedShows() = filesRepository.getSavedFiles()

    fun getShow(id: String) = filesRepository.getFile(id)

}