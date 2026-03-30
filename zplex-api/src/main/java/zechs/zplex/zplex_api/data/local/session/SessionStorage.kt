package zechs.zplex.zplex_api.data.local.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionStorage @Inject constructor(
    @ApplicationContext context: Context
) {

    private val dataStore = context.dataStore

    fun accessTokenFlow(): Flow<String?> {
        return dataStore.data
            .map { it[ACCESS_TOKEN] }
            .distinctUntilChanged()
    }

    fun refreshTokenFlow(): Flow<String?> {
        return dataStore.data
            .map { it[REFRESH_TOKEN] }
            .distinctUntilChanged()
    }

    suspend fun saveAccessToken(token: String) {
        dataStore.edit {
            it[ACCESS_TOKEN] = token
        }
    }

    suspend fun getAccessToken(): String? {
        return dataStore.data.first()[ACCESS_TOKEN]
    }

    suspend fun saveRefreshToken(token: String) {
        dataStore.edit {
            it[REFRESH_TOKEN] = token
        }
    }

    suspend fun getRefreshToken(): String? {
        return dataStore.data.first()[REFRESH_TOKEN]
    }

    suspend fun clearSession() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN)
            it.remove(REFRESH_TOKEN)
        }
    }

    companion object {
        private val Context.dataStore by preferencesDataStore("session")
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}