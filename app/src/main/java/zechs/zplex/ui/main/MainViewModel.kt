package zechs.zplex.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import zechs.zplex.utils.SessionManager
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sessionManager: SessionManager
) : ViewModel() {

    val needToPickFolder = sessionManager.needToPickFolder()

}