package zechs.zplex.utils

import zechs.zplex.BuildConfig

object Constants {
    const val OMDB_API_URL = "https://www.omdbapi.com"
    const val OMDB_API_KEY = BuildConfig.OMDB_API_KEY
    const val TMDB_API_URL = "https://api.themoviedb.org"
    const val TMDB_IMAGE_PREFIX = "https://www.themoviedb.org/t/p"
    const val TMDB_API_KEY = BuildConfig.TMDB_API_KEY

    const val SEARCH_DELAY_AMOUNT = 750L

    const val GUIDE_TO_MAKE_DRIVE_CLIENT = "https://rclone.org/drive/#making-your-own-client-id"

    const val CACHE_TTL_IN_DAYS: Long = 30L

    const val DEFAULT_PAGE_SIZE = 50
}