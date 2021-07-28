package zechs.zplex.utils

class ConverterUtils {
    companion object {
        fun getSize(size: Long): String {
            val s: String
            val kb = size.toString().toDouble() / 1024
            val mb = kb / 1024
            val gb = mb / 1024
            val tb = gb / 1024
            s = when {
                size < 1024L -> "$size Bytes"
                size < 1024L * 1024 -> String.format("%.2f", kb) + " KB"
                size < 1024L * 1024 * 1024 -> String.format("%.2f", mb) + " MB"
                size < 1024L * 1024 * 1024 * 1024 -> String.format("%.2f", gb) + " GB"
                else -> String.format("%.2f", tb) + " TB"
            }
            return s
        }
    }
}