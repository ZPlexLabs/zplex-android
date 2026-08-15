package zechs.zplex.common.utils

/** Builds themoviedb.org image URLs from TMDB relative paths (which include a leading slash). */
object TmdbImage {

    private const val PREFIX = "https://www.themoviedb.org/t/p"

    fun poster(path: String?, size: String = "w342"): String? =
        path?.let { "$PREFIX/$size$it" }

    fun backdrop(path: String?, size: String = "w780"): String? =
        path?.let { "$PREFIX/$size$it" }

    fun logo(path: String?, size: String = "w300"): String? =
        path?.let { "$PREFIX/$size$it" }
}
