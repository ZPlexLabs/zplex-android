package zechs.zplex.feature_settings.kids

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.kids.KidsModeStore
import zechs.zplex.common.ui.mvi.MviViewModel
import javax.inject.Inject

private const val PIN_LENGTH = 4

@HiltViewModel
class KidsModeSetupViewModel @Inject constructor(
    private val kidsModeStore: KidsModeStore
) : MviViewModel<KidsModeSetupState, KidsModeSetupAction, KidsModeSetupEvent>(KidsModeSetupState()) {

    init {
        viewModelScope.launch {
            val hasPin = kidsModeStore.hasPin()
            setState { copy(hasPin = hasPin) }
        }
    }

    override fun onAction(action: KidsModeSetupAction) {
        when (action) {
            KidsModeSetupAction.RequestEnable -> onRequestEnable()
            is KidsModeSetupAction.PinChanged -> setState {
                copy(pin = action.pin.filter { it.isDigit() }.take(PIN_LENGTH), error = null)
            }

            is KidsModeSetupAction.ConfirmPinChanged -> setState {
                copy(confirmPin = action.pin.filter { it.isDigit() }.take(PIN_LENGTH), error = null)
            }

            KidsModeSetupAction.ConfirmSetPin -> confirmSetPin()
            KidsModeSetupAction.DismissSetPinDialog -> setState {
                copy(showSetPinDialog = false, pin = "", confirmPin = "", error = null)
            }
        }
    }

    private fun onRequestEnable() {
        if (currentState.hasPin) {
            enable()
        } else {
            setState { copy(showSetPinDialog = true) }
        }
    }

    private fun confirmSetPin() {
        val state = currentState
        if (state.pin.length != PIN_LENGTH) {
            setState { copy(error = "Enter a $PIN_LENGTH-digit PIN") }
            return
        }
        if (state.pin != state.confirmPin) {
            setState { copy(error = "PINs don't match") }
            return
        }
        viewModelScope.launch {
            kidsModeStore.setPin(state.pin)
            setState { copy(showSetPinDialog = false, pin = "", confirmPin = "") }
            enable()
        }
    }

    private fun enable() {
        viewModelScope.launch {
            kidsModeStore.setEnabled(true)
            sendEvent(KidsModeSetupEvent.Enabled)
        }
    }
}
