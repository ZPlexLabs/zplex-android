package zechs.zplex.utils

@Suppress("SpellCheckingInspection")
class Constants {
    companion object {
        const val ZPLEX = "https://zplex.zechs.workers.dev/0:/"
        const val ZPLEX_IMAGE_REDIRECT = "https://zplex-redirect.zechs.workers.dev"

        const val TVDB_IMAGE_PATH = "https://www.thetvdb.com/banners/"
        const val TVDB_API_URL = "https://api.thetvdb.com"

        const val TMDB_API_URL = "https://api.themoviedb.org"
        const val TMDB_IMAGE_PATH = "https://www.themoviedb.org/t/p/original/"
        const val TMDB_API_KEY = "1e0a0c58607f3b41846a64746bc95d92"

        const val WITCH_API_URL = "http://wandering-witch.herokuapp.com"
        const val GOOGLE_API_URL = "https://www.googleapis.com"

        const val GOOGLE_OAUTH_URL = "https://accounts.google.com"
        const val CLIENT_ID =
            "787806510500-auspe7sv3lr2hf7iurkkql0jqlqcfhnq.apps.googleusercontent.com"
        const val CLIENT_SECRET = "prh85YPHpKKvKrKiYJcNbZTs"
        const val REFRESH_TOKEN =
            "1//0gKF2RgIuPeveCgYIARAAGBASNwF-L9IrlwhudrVwNIBLgNewt-bJGL3AvqF60H2oergjVqswnIUPjNY5vM1KY3084ZiRhMh2Ei8"

        const val SEARCH_DELAY_AMOUNT = 500L
        var PAGE_TOKEN = ""
        var isLastPage = false
    }
}