package zechs.zplex.utils

import zechs.zplex.BuildConfig

@Suppress("SpellCheckingInspection")
object Constants {

    const val TMDB_API_URL = "https://api.themoviedb.org"
    const val TMDB_IMAGE_PREFIX = "https://www.themoviedb.org/t/p"
    const val TMDB_API_KEY = BuildConfig.TMDB_API_KEY

    const val THEMOVIEDB_ID_REGEX = "(movie|tv)/(.*\\d)*"
    const val SEARCH_DELAY_AMOUNT = 750L

}