package zechs.zplex.utils

@Suppress("SpellCheckingInspection")
object Constants {

    const val WITCH_API_URL = "https://wandering-witch.herokuapp.com"

    const val TMDB_API_URL = "https://api.themoviedb.org"
    const val TMDB_IMAGE_PREFIX = "https://www.themoviedb.org/t/p"
    const val TMDB_API_KEY = "1e0a0c58607f3b41846a64746bc95d92"

    const val GOOGLE_API_URL = "https://www.googleapis.com"
    const val GOOGLE_OAUTH_URL = "https://accounts.google.com"

    var ZPLEX = ""
    var ZPLEX_DRIVE_ID = ""
    var ZPLEX_MOVIES_ID = ""
    var ZPLEX_SHOWS_ID = ""
    const val ZPLEX_MUSIC_ID = "16coo3HmIOG9b43cCWAymb7ZX8fHoUDkQ"

    var CLIENT_ID = ""
    var CLIENT_SECRET = ""
    var REFRESH_TOKEN = ""
    var TEMP_TOKEN = ""

    const val SEASON_EPISODE_REGEX = "^.*S(.*\\d)E(.\\d{1,3})"
    const val THEMOVIEDB_ID_REGEX = "(movie|tv)/(.*\\d)*"
    const val SEARCH_DELAY_AMOUNT = 750L

    const val DOCUMENT_PATH = "p79WGSgB7RXlTAIQgxjx"
}