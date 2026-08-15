package zechs.zplex.zplex_api.data.download

import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

data class DownloadRequest(
    val fileId: String,
    val tmdbId: Int,
    val mediaType: MediaType,
    val title: String,
    val subtitle: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val posterPath: String? = null
) {
    val id: String get() = downloadId(mediaType, tmdbId, seasonNumber, episodeNumber)
}

fun downloadId(
    mediaType: MediaType,
    tmdbId: Int,
    seasonNumber: Int?,
    episodeNumber: Int?
): String = when (mediaType) {
    MediaType.SHOW -> "show_${tmdbId}_s${seasonNumber}_e${episodeNumber}"
    MediaType.MOVIE -> "movie_$tmdbId"
}
