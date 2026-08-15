package zechs.zplex.common.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Marker for immutable screen state rendered by a Compose screen. */
interface UiState

/** Marker for one-off side effects (navigation, snackbars) consumed once. */
interface UiEvent

/** Marker for user intents dispatched from the UI to the ViewModel. */
interface UiAction

abstract class MviViewModel<S : UiState, A : UiAction, E : UiEvent>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _events = Channel<E>(Channel.BUFFERED)
    val events: Flow<E> = _events.receiveAsFlow()

    protected val currentState: S get() = _state.value

    abstract fun onAction(action: A)

    protected fun setState(reducer: S.() -> S) = _state.update(reducer)

    protected fun sendEvent(event: E) {
        viewModelScope.launch { _events.send(event) }
    }
}
