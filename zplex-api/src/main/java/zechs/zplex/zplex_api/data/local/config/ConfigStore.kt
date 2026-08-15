package zechs.zplex.zplex_api.data.local.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import zechs.zplex.zplex_api.data.remote.api.config.model.ConfigResponse
import javax.inject.Inject
import javax.inject.Singleton

private val Context.configDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "zplex_config"
)

/** Persists the server [ConfigResponse] so filters and the streaming host survive offline. */
@Singleton
class ConfigStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    moshi: Moshi
) {
    private val adapter = moshi.adapter(ConfigResponse::class.java)

    val config: Flow<ConfigResponse?> = context.configDataStore.data.map { prefs ->
        prefs[CONFIG_KEY]?.let { runCatching { adapter.fromJson(it) }.getOrNull() }
    }

    suspend fun save(config: ConfigResponse) {
        val json = adapter.toJson(config)
        context.configDataStore.edit { prefs -> prefs[CONFIG_KEY] = json }
    }

    private companion object {
        val CONFIG_KEY = stringPreferencesKey("config_json")
    }
}
