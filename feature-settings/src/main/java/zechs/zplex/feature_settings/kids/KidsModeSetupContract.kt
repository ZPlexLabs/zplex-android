package zechs.zplex.feature_settings.kids

import zechs.zplex.common.ui.mvi.UiAction
import zechs.zplex.common.ui.mvi.UiEvent
import zechs.zplex.common.ui.mvi.UiState

data class KidsModeSetupState(
    val hasPin: Boolean = false,
    val showSetPinDialog: Boolean = false,
    val pin: String = "",
    val confirmPin: String = "",
    val error: String? = null
) : UiState

sealed interface KidsModeSetupAction : UiAction {
    data object RequestEnable : KidsModeSetupAction
    data class PinChanged(val pin: String) : KidsModeSetupAction
    data class ConfirmPinChanged(val pin: String) : KidsModeSetupAction
    data object ConfirmSetPin : KidsModeSetupAction
    data object DismissSetPinDialog : KidsModeSetupAction
}

sealed interface KidsModeSetupEvent : UiEvent {
    data object Enabled : KidsModeSetupEvent
}
