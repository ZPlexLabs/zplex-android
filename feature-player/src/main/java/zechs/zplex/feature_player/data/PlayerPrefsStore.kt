package zechs.zplex.feature_player.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Remembers the preferred audio/subtitle language per show so tracks auto-select on load. */
class PlayerPrefsStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val dataStore = context.playerDataStore

    suspend fun audioLang(tmdbId: Int): String? =
        dataStore.data.first()[audioKey(tmdbId)]

    suspend fun subLang(tmdbId: Int): String? =
        dataStore.data.first()[subKey(tmdbId)]

    suspend fun save(tmdbId: Int, audioLang: String?, subLang: String?) {
        if (tmdbId <= 0) return
        dataStore.edit {
            if (audioLang != null) it[audioKey(tmdbId)] = audioLang else it.remove(audioKey(tmdbId))
            if (subLang != null) it[subKey(tmdbId)] = subLang else it.remove(subKey(tmdbId))
        }
    }

    private fun audioKey(tmdbId: Int) = stringPreferencesKey("audio_$tmdbId")
    private fun subKey(tmdbId: Int) = stringPreferencesKey("sub_$tmdbId")

    companion object {
        private val Context.playerDataStore by preferencesDataStore("player_prefs")
    }
}
