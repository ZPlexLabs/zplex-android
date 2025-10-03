package zechs.zplex.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject


class ApiManager @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val dataStore = context.dataStore

    suspend fun saveApi(value: String) {
        val dataStoreKey = stringPreferencesKey(API_KEY)
        dataStore.edit { settings ->
            settings[dataStoreKey] = value
        }
        Log.d(TAG, "saveApi=$value")
    }

    suspend fun fetchApi(): String? {
        val dataStoreKey = stringPreferencesKey(API_KEY)
        val preferences = dataStore.data.first()
        val value = preferences[dataStoreKey]
        Log.d(TAG, "fetchApi=$value")
        return value
    }

    companion object {
        private val Context.dataStore by preferencesDataStore("zplex-api")
        const val TAG = "ApiManager"
        const val API_KEY = "ZPLEX_API"
    }
}