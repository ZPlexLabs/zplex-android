package zechs.zplex.models.tmdb.tv

import androidx.annotation.Keep

@Keep
data class Video(
    val name: String,
    val key: String,
    val site: String
) {
    val thumbUrl get() = "https://i3.ytimg.com/vi/$key/maxresdefault.jpg"
    val watchUrl get() = "https://www.youtube.com/watch?v=$key"
}