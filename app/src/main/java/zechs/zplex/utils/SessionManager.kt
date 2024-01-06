package zechs.zplex.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import zechs.zplex.data.model.drive.DriveClient
import zechs.zplex.data.model.drive.TokenResponse
import javax.inject.Inject


class SessionManager @Inject constructor(
    context: Context,
    private val gson: Gson
) {

    private val sessionStore = context.dataStore

    suspend fun saveClient(client: DriveClient) {
        val dataStoreKey = stringPreferencesKey(DRIVE_CLIENT)
        sessionStore.edit { settings ->
            settings[dataStoreKey] = gson.toJson(client)
        }
        Log.d(TAG, "saveClient: $client")
    }

    suspend fun fetchClient(): DriveClient? {
        val dataStoreKey = stringPreferencesKey(DRIVE_CLIENT)
        val preferences = sessionStore.data.first()
        val value = preferences[dataStoreKey]
        val client: DriveClient? = value?.let {
            val type = object : TypeToken<DriveClient?>() {}.type
            gson.fromJson(value, type)
        }
        Log.d(TAG, "fetchClient: $client")
        return client
    }

    fun fetchDriveClientFlow(): Flow<DriveClient?> {
        return sessionStore.data.map { preferences ->
            val value = preferences[stringPreferencesKey(DRIVE_CLIENT)]
            val client: DriveClient? = value?.let {
                val type = object : TypeToken<DriveClient?>() {}.type
                gson.fromJson(value, type)
            }
            return@map client
        }
    }

    suspend fun saveAccessToken(data: TokenResponse) {
        val dataStoreKey = stringPreferencesKey(ACCESS_TOKEN)
        val currentTimeInSeconds = System.currentTimeMillis() / 1000
        val newData = data.copy(
            expiresIn = currentTimeInSeconds + data.expiresIn
        )
        sessionStore.edit { settings ->
            settings[dataStoreKey] = gson.toJson(newData)
        }
        Log.d(TAG, "saveAccessToken: $data")
    }

    suspend fun fetchAccessToken(): TokenResponse? {
        val dataStoreKey = stringPreferencesKey(ACCESS_TOKEN)
        val preferences = sessionStore.data.first()
        val value = preferences[dataStoreKey]
        val login: TokenResponse? = value?.let {
            val type = object : TypeToken<TokenResponse?>() {}.type
            gson.fromJson(value, type)
        }
        Log.d(TAG, "fetchAccessToken: $login")
        return login
    }

    suspend fun saveRefreshToken(refreshToken: String) {
        val dataStoreKey = stringPreferencesKey(REFRESH_TOKEN)
        sessionStore.edit { settings ->
            settings[dataStoreKey] = refreshToken
        }
        Log.d(TAG, "saveRefreshToken: $refreshToken")
    }

    suspend fun fetchRefreshToken(): String? {
        val dataStoreKey = stringPreferencesKey(REFRESH_TOKEN)
        val preferences = sessionStore.data.first()
        val value = preferences[dataStoreKey]
        Log.d(TAG, "fetchRefreshToken: $value")
        return value
    }

    fun isLoggedIn(): Flow<Boolean> {
        return sessionStore.data.map { preferences ->
            val value = preferences[stringPreferencesKey(ACCESS_TOKEN)]
            return@map value != null
        }
    }

    suspend fun saveMovieFolder(movieFolderId: String) {
        val dataStoreKey = stringPreferencesKey(MOVIE_FOLDER)
        sessionStore.edit { settings ->
            settings[dataStoreKey] = movieFolderId
        }
        Log.d(TAG, "saveMovieFolder: $movieFolderId")
    }

    suspend fun fetchMovieFolder(): String? {
        val dataStoreKey = stringPreferencesKey(MOVIE_FOLDER)
        val preferences = sessionStore.data.first()
        val value = preferences[dataStoreKey]
        Log.d(TAG, "fetchMovieFolder: $value")
        return value
    }

    suspend fun saveShowsFolder(showsFolderId: String) {
        val dataStoreKey = stringPreferencesKey(SHOWS_FOLDER)
        sessionStore.edit { settings ->
            settings[dataStoreKey] = showsFolderId
        }
        Log.d(TAG, "saveMovieFolder: $showsFolderId")
    }

    suspend fun fetchShowsFolder(): String? {
        val dataStoreKey = stringPreferencesKey(SHOWS_FOLDER)
        val preferences = sessionStore.data.first()
        val value = preferences[dataStoreKey]
        Log.d(TAG, "fetchShowsFolder: $value")
        return value
    }

    fun fetchMovieFolderFlow(): Flow<String?> {
        return sessionStore.data.map { preferences ->
            preferences[stringPreferencesKey(MOVIE_FOLDER)]
        }
    }

    fun fetchShowsFolderFlow(): Flow<String?> {
        return sessionStore.data.map { preferences ->
            preferences[stringPreferencesKey(SHOWS_FOLDER)]
        }
    }

    suspend fun resetDataStore() {
        sessionStore.edit { it.clear() }
    }

    companion object {
        private val Context.dataStore by preferencesDataStore(
            "DRIVE_SESSION"
        )
        const val TAG = "SessionManager"
        const val DRIVE_CLIENT = "DRIVE_CLIENT"
        const val ACCESS_TOKEN = "ACCESS_TOKEN"
        const val REFRESH_TOKEN = "REFRESH_TOKEN"
        const val MOVIE_FOLDER = "MOVIE_FOLDER"
        const val SHOWS_FOLDER = "SHOWS_FOLDER"
    }

}