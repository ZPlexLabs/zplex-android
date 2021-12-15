package zechs.zplex.utils

@Suppress("SpellCheckingInspection")
object Constants {
    const val ZPLEX = "https://zplex.zechs.workers.dev/0:/"
    const val ZPLEX_IMAGE_REDIRECT = "https://zplex-redirect.zechs.workers.dev"

    const val TVDB_IMAGE_PATH = "https://www.thetvdb.com/banners/"
    const val TVDB_API_URL = "https://api.thetvdb.com"

    const val TMDB_API_URL = "https://api.themoviedb.org"
    const val TMDB_IMAGE_PREFIX = "https://www.themoviedb.org/t/p"
    const val TMDB_API_KEY = "1e0a0c58607f3b41846a64746bc95d92"

    const val WITCH_API_URL = "http://wandering-witch.herokuapp.com"
    const val GOOGLE_API_URL = "https://www.googleapis.com"

    const val GOOGLE_OAUTH_URL = "https://accounts.google.com"
    const val CLIENT_ID = "787806510500-auspe7sv3lr2hf7iurkkql0jqlqcfhnq.apps.googleusercontent.com"
    const val CLIENT_SECRET = "prh85YPHpKKvKrKiYJcNbZTs"
    const val REFRESH_TOKEN =
        "1//0gBDbtsXxCEGKCgYIARAAGBASNwF-L9IrEv60PzcI6tzrhRQf7txK8MooeHtkLadSXZW2cz_56SvCejWVLlpc43jrNXOMxmVNy14"
    const val TEMP_TOKEN =
        "ya29.a0ARrdaM-eMe9zOKy57ZF4rrtIGPTxtVi1S97nRu7mIed0qAWfVkrqPxpL24vRooTu9H8Z-HYZbLXISrlTsglo7s_WuMFOH20EuRR6sVQ1KkqdvZ9wT5I33hmUYxJtu_5M7miNnstg2Zi1Tt0Gzcz3chtMFB1b"

    const val SEARCH_DELAY_AMOUNT = 500L
    var PAGE_TOKEN = ""
    var isLastPage = false
    const val regexFile = "^S(.*[0-9])E(.*[0-9])( - )(.*)(.mkv)"
    const val regexShow = "^(.*[0-9])( - )(.*)( - )(TV|Movie)"
}