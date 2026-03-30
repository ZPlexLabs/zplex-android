package zechs.zplex.googledrive.data.local


import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import zechs.zplex.googledrive.data.local.DataStoreConstants.DRIVE_SESSION_KEY
import zechs.zplex.googledrive.data.remote.api.token.model.TokenResponse
import javax.inject.Inject

private val Context.driveDataStore by preferencesDataStore(name = DRIVE_SESSION_KEY)

class TokenStore @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {

    private val dataStore = context.driveDataStore

    suspend fun saveAccessToken(data: TokenResponse) {
        val key = stringPreferencesKey(ACCESS_TOKEN)
        val currentTimeInSeconds = System.currentTimeMillis() / 1000

        val newData = data.copy(
            expiresIn = currentTimeInSeconds + data.expiresIn
        )

        dataStore.edit {
            it[key] = gson.toJson(newData)
        }

        Log.d(TAG, "saveAccessToken: $data")
    }

    suspend fun fetchAccessToken(): TokenResponse? {
        val key = stringPreferencesKey(ACCESS_TOKEN)
        val value = dataStore.data.first()[key]

        val token = value?.let {
            gson.fromJson(it, TokenResponse::class.java)
        }

        Log.d(TAG, "fetchAccessToken: $token")
        return token
    }

    suspend fun saveRefreshToken(refreshToken: String) {
        val key = stringPreferencesKey(REFRESH_TOKEN)
        dataStore.edit {
            it[key] = refreshToken
        }
        Log.d(TAG, "saveRefreshToken: $refreshToken")
    }

    suspend fun fetchRefreshToken(): String? {
        val key = stringPreferencesKey(REFRESH_TOKEN)
        val value = dataStore.data.first()[key]
        Log.d(TAG, "fetchRefreshToken: $value")
        return value
    }

    fun isLoggedIn(): Flow<Boolean> {
        val key = stringPreferencesKey(ACCESS_TOKEN)
        return dataStore.data.map {
            it[key] != null
        }
    }

    companion object {
        private const val TAG = "TokenStore"
        private const val ACCESS_TOKEN = "ACCESS_TOKEN"
        private const val REFRESH_TOKEN = "REFRESH_TOKEN"
    }
}