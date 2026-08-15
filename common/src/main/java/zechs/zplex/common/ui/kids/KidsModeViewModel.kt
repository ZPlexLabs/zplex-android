package zechs.zplex.common.ui.kids

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KidsModeViewModel @Inject constructor(
    private val kidsModeStore: KidsModeStore
) : ViewModel() {

    val isEnabled: StateFlow<Boolean> = kidsModeStore.isEnabledFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    suspend fun verifyPinAndExit(pin: String): Boolean {
        val correct = kidsModeStore.verifyPin(pin)
        if (correct) kidsModeStore.setEnabled(false)
        return correct
    }

    fun enable() {
        viewModelScope.launch { kidsModeStore.setEnabled(true) }
    }
}
