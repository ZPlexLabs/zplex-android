package zechs.zplex.zplex_api.data.local.user

import android.content.Context
import android.util.Log
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
                val json = preferences[USER_KEY]

                if (json == null) {
                    Log.d(TAG, "userFlow -> No user found")
                    return@map null
                }

                try {
                    val user = gson.fromJson(json, User::class.java)
                    Log.d(TAG, "userFlow -> User loaded: ${user.username}")
                    user
                } catch (e: Exception) {
                    Log.e(TAG, "userFlow -> Failed to parse user JSON", e)
                    null
                }
            }
            .distinctUntilChanged()
    }

    suspend fun saveUser(user: User) {
        try {
            val json = gson.toJson(user)

            dataStore.edit {
                it[USER_KEY] = json
            }

            Log.d(TAG, "saveUser -> User saved: ${user.username}")

        } catch (e: Exception) {
            Log.e(TAG, "saveUser -> Failed to save user", e)
        }
    }

    suspend fun getUser(): User? {
        return try {
            val json = dataStore.data.first()[USER_KEY]

            if (json == null) {
                Log.d(TAG, "getUser -> No user found")
                return null
            }

            val user = gson.fromJson(json, User::class.java)
            Log.d(TAG, "getUser -> User loaded: ${user.username}")
            user

        } catch (e: Exception) {
            Log.e(TAG, "getUser -> Failed to read user", e)
            null
        }
    }

    suspend fun clearUser() {
        try {
            dataStore.edit {
                it.remove(USER_KEY)
            }
            Log.d(TAG, "clearUser -> User cleared")

        } catch (e: Exception) {
            Log.e(TAG, "clearUser -> Failed to clear user", e)
        }
    }

    companion object {
        private const val TAG = "UserStorage"

        private val Context.dataStore by preferencesDataStore("user")
        private val USER_KEY = stringPreferencesKey("user")
    }
}