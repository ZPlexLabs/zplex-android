package zechs.zplex.common.utils

import java.util.Locale

object SizeUtils {

    fun format(size: Long): String {
        val kb = size.toString().toDouble() / 1024
        val mb = kb / 1024
        val gb = mb / 1024
        val tb = gb / 1024
        return when {
            size < 1024L -> "$size Bytes"
            size < 1024L * 1024 -> String.format(Locale.ENGLISH, "%.2f", kb) + " KB"
            size < 1024L * 1024 * 1024 -> String.format(Locale.ENGLISH, "%.2f", mb) + " MB"
            size < 1024L * 1024 * 1024 * 1024 -> String.format(Locale.ENGLISH, "%.2f", gb) + " GB"
            else -> String.format(Locale.ENGLISH, "%.2f", tb) + " TB"
        }
    }
}