package zechs.zplex.googledrive.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import zechs.zplex.googledrive.data.local.DataStoreConstants.DRIVE_SESSION_KEY
import javax.inject.Inject

private val Context.driveDataStore by preferencesDataStore(name = DRIVE_SESSION_KEY)

class DriveFoldersStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val dataStore = context.driveDataStore

    suspend fun saveMovieFolder(id: String) {
        val key = stringPreferencesKey(MOVIE_FOLDER)
        dataStore.edit {
            it[key] = id
        }
        Log.d(TAG, "saveMovieFolder: $id")
    }

    suspend fun fetchMovieFolder(): String? {
        val key = stringPreferencesKey(MOVIE_FOLDER)
        val value = dataStore.data.first()[key]
        Log.d(TAG, "fetchMovieFolder: $value")
        return value
    }

    suspend fun saveShowsFolder(id: String) {
        val key = stringPreferencesKey(SHOWS_FOLDER)
        dataStore.edit {
            it[key] = id
        }
        Log.d(TAG, "saveShowsFolder: $id")
    }

    suspend fun fetchShowsFolder(): String? {
        val key = stringPreferencesKey(SHOWS_FOLDER)
        val value = dataStore.data.first()[key]
        Log.d(TAG, "fetchShowsFolder: $value")
        return value
    }

    fun movieFolderFlow(): Flow<String?> {
        val key = stringPreferencesKey(MOVIE_FOLDER)
        return dataStore.data.map { it[key] }
    }

    fun showsFolderFlow(): Flow<String?> {
        val key = stringPreferencesKey(SHOWS_FOLDER)
        return dataStore.data.map { it[key] }
    }

    companion object {
        private const val TAG = "DriveFoldersStore"
        private const val MOVIE_FOLDER = "MOVIE_FOLDER"
        private const val SHOWS_FOLDER = "SHOWS_FOLDER"
    }
}