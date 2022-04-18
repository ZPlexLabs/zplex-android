package zechs.zplex.utils

@Suppress("SpellCheckingInspection")
object Constants {
    // 'zechs.wandering-witch' encoded in base6
    const val ENCRYPTION_KEY = "emVjaHMud2FuZGVyaW5nLndpdGNo"

    const val ZPLEX_API_URL = "https://zplex-api.herokuapp.com"

    const val TMDB_API_URL = "https://api.themoviedb.org"
    const val TMDB_IMAGE_PREFIX = "https://www.themoviedb.org/t/p"
    const val TMDB_API_KEY = "1e0a0c58607f3b41846a64746bc95d92"

    const val THEMOVIEDB_ID_REGEX = "(movie|tv)/(.*\\d)*"
    const val SEARCH_DELAY_AMOUNT = 750L

    const val VERSION_CODE_KEY = "latest_app_version"
    const val DRIVE_ZPLEX_RELEASES =
        "https://drive.google.com/drive/folders/1ZMnIVlpJPjGNNfXDFANMVOYQYTVlvFZ4"
}