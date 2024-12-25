package zechs.zplex.utils.ext

import android.util.Log
import java.io.File

fun File.deleteIfExists() {
    if (exists()) delete()
}

fun File.deleteIfExistsSafely() {
    try {
        if (exists()) delete()
    } catch (e: Exception) {
        Log.d("FileExt", "deleteIfExistsSafely: " + e.message)
    }
}