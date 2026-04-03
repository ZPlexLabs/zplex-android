package zechs.zplex.feature_home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import zechs.zplex.feature_home.ui.HomeFragment.Companion.TAG
import zechs.zplex.zplex_api.data.local.user.User
import zechs.zplex.zplex_api.data.local.user.UserStorage
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    userStorage: UserStorage
) : ViewModel() {

    val user: StateFlow<User?> = userStorage.userFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun logout() {
        Log.d(TAG, "Log out user: ${user.value?.username}")
    }
}