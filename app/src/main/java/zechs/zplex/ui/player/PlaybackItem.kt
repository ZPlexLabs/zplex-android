package zechs.zplex.ui.player


interface PlaybackItem {
    val tmdbId: Int
    val title: String
    val posterPath: String?
    val fileId: String
    var next: PlaybackItem?
    var prev: PlaybackItem?
}

data class Movie(
    override val tmdbId: Int,
    override val title: String,
    override val posterPath: String?,
    override val fileId: String,
    override var next: PlaybackItem? = null,
    override var prev: PlaybackItem? = null,
) : PlaybackItem


data class Show(
    override val tmdbId: Int,
    override val title: String,
    override val posterPath: String?,
    override val fileId: String,
    override var next: PlaybackItem? = null,
    override var prev: PlaybackItem? = null,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeTitle: String?,
) : PlaybackItem

data class GsonPlaybackItem(
    val tmdbId: Int,
    val title: String,
    val fileId: String,
    val posterPath: String?,
    val next: Int?,
    val prev: Int?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val episodeTitle: String?,
) {
    fun toPlaybackItem(): PlaybackItem {
        return if (seasonNumber != null && episodeNumber != null && episodeTitle != null) {
            Show(tmdbId, title, posterPath, fileId, null, null, seasonNumber, episodeNumber, episodeTitle)
        } else {
            Movie(tmdbId, title, posterPath, fileId, null, null)
        }
    }
}