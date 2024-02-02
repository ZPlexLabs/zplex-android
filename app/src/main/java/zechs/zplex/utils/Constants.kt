package zechs.zplex.utils

import zechs.zplex.BuildConfig

object Constants {

    const val TMDB_API_URL = "https://api.themoviedb.org"
    const val TMDB_IMAGE_PREFIX = "https://www.themoviedb.org/t/p"
    const val TMDB_API_KEY = BuildConfig.TMDB_API_KEY

    const val GOOGLE_API = "https://www.googleapis.com"
    const val DRIVE_API = "${GOOGLE_API}/drive/v3"
    const val GOOGLE_ACCOUNTS_URL = "https://accounts.google.com"

    const val SEARCH_DELAY_AMOUNT = 750L

    const val GUIDE_TO_MAKE_DRIVE_CLIENT = "https://rclone.org/drive/#making-your-own-client-id"
}