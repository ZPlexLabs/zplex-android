package zechs.zplex.googledrive.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import zechs.zplex.googledrive.data.local.DataStoreConstants.DRIVE_SESSION_KEY
import javax.inject.Inject

private val Context.driveDataStore by preferencesDataStore(name = DRIVE_SESSION_KEY)

class SessionCleaner @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.driveDataStore

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

}