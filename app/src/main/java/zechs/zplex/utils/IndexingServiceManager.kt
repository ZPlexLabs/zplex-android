package zechs.zplex.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class IndexingServiceManager @Inject constructor(
    context: Context
) {
    private val sessionStore = context.dataStore

    private suspend fun updateLastRun() {
        val dataStoreKey = stringPreferencesKey(LAST_RUN)
        val timestamp = formatDate(Calendar.getInstance().timeInMillis)
        sessionStore.edit { settings ->
            settings[dataStoreKey] = timestamp
        }
        Log.d(TAG, "Last run updated to $timestamp")
    }

    private suspend fun getLastRun(): String? {
        val dataStoreKey = stringPreferencesKey(LAST_RUN)
        val preferences = sessionStore.data.first()
        val value = preferences[dataStoreKey]
        Log.d(TAG, "Last run is $value")
        return value
    }

    private fun formatDate(timeInMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        // format: 10:25 PM 6th Jan, 2024
        val dateFormat = SimpleDateFormat("hh:mm a d MMM, yyyy", Locale.ENGLISH)
        return dateFormat.format(calendar.time)
    }

    fun getStatus(): Flow<IndexingServiceState> {
        val dataStoreKey = stringPreferencesKey(CURRENT_STATUS)
        return sessionStore.data.map { preferences ->
            val value = preferences[dataStoreKey]
            Log.d(TAG, "Status is $value")
            when (IndexingServiceAction.valueOf(value ?: IndexingServiceAction.UNKNOWN.name)) {
                IndexingServiceAction.START -> IndexingServiceState.Running
                IndexingServiceAction.STOP -> {
                    IndexingServiceState.Stopped(getLastRun())
                }

                IndexingServiceAction.UNKNOWN -> IndexingServiceState.Unknown
            }
        }.catch { e ->
            Log.e(TAG, "Error getting status", e)
            emit(IndexingServiceState.Unknown)
        }
    }

    suspend fun setStatus(action: IndexingServiceAction) {
        val dataStoreKey = stringPreferencesKey(CURRENT_STATUS)
        if (action == IndexingServiceAction.STOP) {
            updateLastRun()
        }
        sessionStore.edit { settings ->
            settings[dataStoreKey] = action.name
        }
        Log.d(TAG, "Status set to ${action.name}")
    }

    companion object {
        private val Context.dataStore by preferencesDataStore(
            "INDEXING_SERVICE"
        )
        const val TAG = "IndexingServiceManager"
        const val LAST_RUN = "LAST_RUN"
        const val CURRENT_STATUS = "CURRENT_STATUS"
    }
}

sealed interface IndexingServiceState {
    data object Unknown : IndexingServiceState
    data object Running : IndexingServiceState
    data class Stopped(val lastRun: String?) : IndexingServiceState
}

enum class IndexingServiceAction {
    START, STOP, UNKNOWN
}