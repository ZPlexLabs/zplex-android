package zechs.zplex.common.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePrefsStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val dataStore = context.themeDataStore

    fun themeModeFlow(): Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    companion object {
        private val Context.themeDataStore by preferencesDataStore("theme_prefs")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
