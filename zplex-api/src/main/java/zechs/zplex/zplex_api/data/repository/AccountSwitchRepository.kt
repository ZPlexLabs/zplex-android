package zechs.zplex.zplex_api.data.repository

import kotlinx.coroutines.flow.Flow
import zechs.zplex.zplex_api.data.local.accounts.SavedAccount
import zechs.zplex.zplex_api.data.local.accounts.SavedAccountsStore
import zechs.zplex.zplex_api.data.local.session.SessionStorage
import zechs.zplex.zplex_api.data.local.user.User
import zechs.zplex.zplex_api.data.local.user.UserStorage
import javax.inject.Inject

/** Switches the active session among saved accounts without a full re-login. */
class AccountSwitchRepository @Inject constructor(
    private val savedAccountsStore: SavedAccountsStore,
    private val sessionStorage: SessionStorage,
    private val userStorage: UserStorage
) {

    fun savedAccounts(): Flow<List<SavedAccount>> = savedAccountsStore.savedAccountsFlow()

    suspend fun switchTo(account: SavedAccount) {
        sessionStorage.saveAccessToken(account.accessToken)
        sessionStorage.saveRefreshToken(account.refreshToken)
        userStorage.saveUser(
            User(
                firstName = account.firstName,
                lastName = account.lastName,
                username = account.username,
                capabilities = account.capabilities,
                isAdult = account.isAdult,
                tokenType = account.tokenType
            )
        )
    }

    /** Signs out the active session (kept in the saved list) so a different account can log in. */
    suspend fun addAccount() {
        sessionStorage.clearSession()
        userStorage.clearUser()
    }

    suspend fun removeAccount(username: String) {
        val activeUsername = userStorage.getUser()?.username
        savedAccountsStore.remove(username)
        if (activeUsername == username) {
            sessionStorage.clearSession()
            userStorage.clearUser()
        }
    }
}
