package zechs.zplex.zplex_api.data.local.user

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserStorage @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {

    private val dataStore = context.dataStore

    fun userFlow(): Flow<User?> {
        return dataStore.data
            .map { preferences ->
                preferences[USER_KEY]?.let { json ->
                    try {
                        gson.fromJson(json, User::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .distinctUntilChanged()
    }

    suspend fun saveUser(user: User) {
        dataStore.edit {
            it[USER_KEY] = gson.toJson(user)
        }
    }

    suspend fun getUser(): User? {
        val json = dataStore.data.first()[USER_KEY] ?: return null
        return gson.fromJson(json,User::class.java)
    }

    companion object {
        private val Context.dataStore by preferencesDataStore("user")
        private val USER_KEY = stringPreferencesKey("user")
    }
}