package zechs.zplex.zplex_api.data.local.accounts

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Multiple saved sessions for on-device profile switching (add/remove account without full re-login). */
@Singleton
class SavedAccountsStore @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {

    private val dataStore = context.savedAccountsDataStore

    fun savedAccountsFlow(): Flow<List<SavedAccount>> = dataStore.data.map { readAccounts(it[ACCOUNTS_KEY]) }

    suspend fun getAll(): List<SavedAccount> = readAccounts(dataStore.data.first()[ACCOUNTS_KEY])

    suspend fun upsert(account: SavedAccount) {
        dataStore.edit { prefs ->
            val accounts = readAccounts(prefs[ACCOUNTS_KEY])
                .filterNot { it.username == account.username } + account
            prefs[ACCOUNTS_KEY] = gson.toJson(accounts)
        }
    }

    suspend fun remove(username: String) {
        dataStore.edit { prefs ->
            val accounts = readAccounts(prefs[ACCOUNTS_KEY]).filterNot { it.username == username }
            prefs[ACCOUNTS_KEY] = gson.toJson(accounts)
        }
    }

    private fun readAccounts(json: String?): List<SavedAccount> {
        if (json.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<SavedAccount>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private val Context.savedAccountsDataStore by preferencesDataStore("saved_accounts")
        private val ACCOUNTS_KEY = stringPreferencesKey("accounts")
    }
}
