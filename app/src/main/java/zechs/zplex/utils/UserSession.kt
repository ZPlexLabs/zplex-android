package zechs.zplex.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import zechs.zplex.data.model.User
import zechs.zplex.data.model.config.Capability
import zechs.zplex.data.model.config.ConfigResponse
import javax.inject.Inject

class UserSession @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {

    private val dataStore = context.dataStore

    suspend fun saveUserSession(user: User) {
        val dataStoreKey = stringPreferencesKey(USER_SESSION_KEY)
        dataStore.edit { settings ->
            settings[dataStoreKey] = gson.toJson(user)
        }
        Log.d(TAG, "User saved: $user")
    }

    suspend fun fetchUserSession(): User? {
        val dataStoreKey = stringPreferencesKey(USER_SESSION_KEY)
        val preferences = dataStore.data.first()
        val value = preferences[dataStoreKey]
        val user: User? = value?.let {
            val type = object : TypeToken<User?>() {}.type
            gson.fromJson(value, type)
        }
        Log.d(TAG, "User fetched: $user")
        return user
    }

    suspend fun saveAccessToken(accessToken: String) {
        val dataStoreKey = stringPreferencesKey(USER_ACCESS_TOKEN_KEY)
        dataStore.edit { settings ->
            settings[dataStoreKey] = accessToken
        }
        Log.d(TAG, "User access token saved: ${accessToken.take(25)}...")
    }

    suspend fun fetchAccessToken(): String? {
        val dataStoreKey = stringPreferencesKey(USER_ACCESS_TOKEN_KEY)
        val preferences = dataStore.data.first()
        val value = preferences[dataStoreKey]
        Log.d(TAG, "Fetched user access token: ${value?.take(25)}...")
        return value
    }

    suspend fun saveRefreshToken(refreshToken: String) {
        val dataStoreKey = stringPreferencesKey(USER_REFRESH_TOKEN_KEY)
        dataStore.edit { settings ->
            settings[dataStoreKey] = refreshToken
        }
        Log.d(TAG, "User refresh token saved: ${refreshToken.take(25)}...")
    }

    suspend fun fetchRefreshToken(): String? {
        val dataStoreKey = stringPreferencesKey(USER_REFRESH_TOKEN_KEY)
        val preferences = dataStore.data.first()
        val value = preferences[dataStoreKey]
        Log.d(TAG, "Fetched user refresh token: ${value?.take(25)}...")
        return value
    }

    suspend fun saveConfig(configResponse: ConfigResponse) {
        val dataStoreKey = stringPreferencesKey(CONFIG_KEY)
        dataStore.edit { settings ->
            settings[dataStoreKey] = gson.toJson(configResponse)
        }
        Log.d(TAG, "Config saved")
    }

    suspend fun fetchConfig(): ConfigResponse? {
        val dataStoreKey = stringPreferencesKey(CONFIG_KEY)
        val preferences = dataStore.data.first()
        val value = preferences[dataStoreKey]
        val config: ConfigResponse? = value?.let {
            val type = object : TypeToken<ConfigResponse?>() {}.type
            gson.fromJson(value, type)
        }
        Log.d(TAG, "Config fetched")
        return config
    }

    suspend fun saveCapabilities(capabilities: List<Capability>) {
        val dataStoreKey = stringPreferencesKey(CAPABILITIES_KEY)
        dataStore.edit { settings ->
            settings[dataStoreKey] = gson.toJson(capabilities)
        }
        Log.d(TAG, "${capabilities.size} capabilities saved")
    }

    suspend fun fetchCapabilities(): List<Capability>? {
        val dataStoreKey = stringPreferencesKey(CAPABILITIES_KEY)
        val preferences = dataStore.data.first()
        val value = preferences[dataStoreKey]
        val capabilities: List<Capability>? = value?.let {
            val type = object : TypeToken<List<Capability>?>() {}.type
            gson.fromJson(value, type)
        }
        Log.d(TAG, "${capabilities?.size ?: 0} capabilities fetched")
        return capabilities
    }

    companion object {
        private val Context.dataStore by preferencesDataStore("user-session")
        private const val TAG = "UserSession"
        private const val USER_SESSION_KEY = "USER_SESSION"
        private const val USER_ACCESS_TOKEN_KEY = "USER_ACCESS_TOKEN_KEY"
        private const val USER_REFRESH_TOKEN_KEY = "USER_REFRESH_TOKEN_KEY"
        private const val CONFIG_KEY = "CONFIG_KEY"
        private const val CAPABILITIES_KEY = "CAPABILITIES_KEY"
    }
}