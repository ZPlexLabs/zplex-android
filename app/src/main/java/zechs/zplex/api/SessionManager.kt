package zechs.zplex.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import zechs.zplex.R

class SessionManager(context: Context) {

    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    private val accessToken = "access_token"

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(accessToken, token)
        editor.apply()
        Log.d("saveAuthToken", token)
    }

    fun fetchAuthToken(): String {
        Log.d("fetchAuthToken", prefs.getString(accessToken, "").toString())
        return prefs.getString(accessToken, "").toString()
    }
}