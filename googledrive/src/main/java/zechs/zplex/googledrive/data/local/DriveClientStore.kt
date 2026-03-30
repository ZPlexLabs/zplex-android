package zechs.zplex.googledrive.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import zechs.zplex.googledrive.data.local.DataStoreConstants.DRIVE_SESSION_KEY
import zechs.zplex.googledrive.data.model.DriveClient
import javax.inject.Inject

private val Context.driveDataStore by preferencesDataStore(name = DRIVE_SESSION_KEY)

class DriveClientStore @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {

    private val dataStore = context.driveDataStore

    suspend fun save(client: DriveClient) {
        val key = stringPreferencesKey(DRIVE_CLIENT)
        dataStore.edit {
            it[key] = gson.toJson(client)
        }
        Log.d(TAG, "saveClient: $client")
    }

    suspend fun fetch(): DriveClient? {
        val key = stringPreferencesKey(DRIVE_CLIENT)
        val value = dataStore.data.first()[key]

        val client = value?.let {
            gson.fromJson(it, DriveClient::class.java)
        }

        Log.d(TAG, "fetchClient: $client")
        return client
    }

    fun flow(): Flow<DriveClient?> {
        return dataStore.data.map { preferences ->
            val value = preferences[stringPreferencesKey(DRIVE_CLIENT)]
            val client: DriveClient? = value?.let {
                val type = object : TypeToken<DriveClient?>() {}.type
                gson.fromJson(value, type)
            }
            return@map client
        }
    }

    companion object {
        private const val TAG = "DriveClientStore"
        private const val DRIVE_CLIENT = "DRIVE_CLIENT"
    }
}