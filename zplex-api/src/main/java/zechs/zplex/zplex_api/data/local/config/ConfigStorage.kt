package zechs.zplex.zplex_api.data.local.config

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import zechs.zplex.zplex_api.data.remote.api.config.model.Capability
import zechs.zplex.zplex_api.data.remote.api.config.model.ConfigResponse
import javax.inject.Inject

class ConfigStorage @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {

    private val dataStore = context.dataStore

    suspend fun saveConfig(config: ConfigResponse) {
        dataStore.edit {
            it[CONFIG_KEY] = gson.toJson(config)
        }
    }

    suspend fun getConfig(): ConfigResponse? {
        val json = dataStore.data.first()[CONFIG_KEY] ?: return null
        return gson.fromJson(json, ConfigResponse::class.java)
    }

    suspend fun saveCapabilities(capabilities: List<Capability>) {
        dataStore.edit {
            it[CAPABILITIES_KEY] = gson.toJson(capabilities)
        }
    }

    suspend fun getCapabilities(): List<Capability>? {
        val json = dataStore.data.first()[CAPABILITIES_KEY] ?: return null
        return gson.fromJson(
            json,
            Array<Capability>::class.java
        )?.toList()
    }

    companion object {
        private val Context.dataStore by preferencesDataStore("config")
        private val CONFIG_KEY = stringPreferencesKey("config")
        private val CAPABILITIES_KEY = stringPreferencesKey("capabilities")
    }
}