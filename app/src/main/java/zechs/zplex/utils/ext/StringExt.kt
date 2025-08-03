package zechs.zplex.utils.ext

fun String?.ifNullOrEmpty(block: () -> String): String {
    return if (this.isNullOrEmpty()) {
        block()
    } else {
        this
    }
}

inline fun <T> String.nullIfNAOrElse(block: (String) -> T): T? =
    if (this == "N/A") null else block(this)