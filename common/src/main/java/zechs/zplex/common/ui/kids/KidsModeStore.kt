package zechs.zplex.common.ui.kids

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/** Restricted "kids mode": simplified nav + a PIN to exit. The PIN is hashed, never stored in plaintext. */
@Singleton
class KidsModeStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val dataStore = context.kidsModeDataStore

    fun isEnabledFlow(): Flow<Boolean> = dataStore.data.map { it[ENABLED] ?: false }

    fun hasPinFlow(): Flow<Boolean> = dataStore.data.map { !it[PIN_HASH].isNullOrEmpty() }

    suspend fun hasPin(): Boolean = !dataStore.data.first()[PIN_HASH].isNullOrEmpty()

    suspend fun setPin(pin: String) {
        dataStore.edit { it[PIN_HASH] = hash(pin) }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val stored = dataStore.data.first()[PIN_HASH] ?: return false
        return stored == hash(pin)
    }

    suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { it[ENABLED] = enabled }
    }

    private fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val Context.kidsModeDataStore by preferencesDataStore("kids_mode")
        private val ENABLED = booleanPreferencesKey("enabled")
        private val PIN_HASH = stringPreferencesKey("pin_hash")
    }
}
