package zechs.zplex.utils.ext

fun String?.ifNullOrEmpty(block: () -> String): String {
    return if (this.isNullOrEmpty()) {
        block()
    } else {
        this
    }
}