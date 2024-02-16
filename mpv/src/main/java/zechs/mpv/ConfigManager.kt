package zechs.mpv

import android.content.Context
import android.util.Log
import java.util.Locale

class ConfigManager(context: Context) {
    companion object {
        private const val TAG = "ConfigManager"
        private const val PREFERENCES_NAME = "MPV_USER_SETTINGS"
        private const val DEFAULT_CACHE_SIZE = 64L * 1024 * 1024 // 64MB
    }

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private enum class ConfigKeys {
        CACHE_SIZE,
        DEFAULT_AUDIO_LANG,
        DEFAULT_SUBTITLE_LANG
    }

    fun putCacheSize(cacheSize: Long) {
        Log.d(TAG, "Setting cache size: $cacheSize")
        with(preferences.edit()) {
            putLong(ConfigKeys.CACHE_SIZE.name, cacheSize)
            apply()
        }
    }

    fun getCacheSize(): Long {
        val cacheSize = preferences.getLong(ConfigKeys.CACHE_SIZE.name, DEFAULT_CACHE_SIZE)
        Log.d(TAG, "Retrieved cache size: $cacheSize")
        return cacheSize
    }

    fun putDefaultAudioLang(lang: Locale) {
        Log.d(TAG, "Setting default audio language: ${lang.isO3Language}")
        with(preferences.edit()) {
            putString(ConfigKeys.DEFAULT_AUDIO_LANG.name, lang.isO3Language)
            apply()
        }
    }

    fun getDefaultAudioLang(): Locale {
        val langString = preferences.getString(
            ConfigKeys.DEFAULT_AUDIO_LANG.name,
            Locale.getDefault().isO3Language
        )!!
        val defaultAudioLang = Locale(langString)
        Log.d(TAG, "Retrieved default audio language: $defaultAudioLang")
        return defaultAudioLang
    }

    fun putDefaultSubtitleLang(lang: Locale) {
        Log.d(TAG, "Setting default subtitle language: ${lang.isO3Language}")
        with(preferences.edit()) {
            putString(ConfigKeys.DEFAULT_SUBTITLE_LANG.name, Locale.getDefault().isO3Language)
            apply()
        }
    }

    fun getDefaultSubtitleLang(): Locale {
        val langString = preferences.getString(
            ConfigKeys.DEFAULT_SUBTITLE_LANG.name,
            Locale.getDefault().isO3Language
        )!!
        val defaultSubtitleLang = Locale(langString)
        Log.d(TAG, "Retrieved default subtitle language: $defaultSubtitleLang")
        return defaultSubtitleLang
    }

}