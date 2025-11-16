package zechs.zplex.ui.player

sealed interface MediaMetadataData {
    val title: String
    val posterPath: String?
    val durationSecs: Int?
}

data class MovieMetadata(
    override val title: String,
    override val posterPath: String? = null,
    override val durationSecs: Int? = null,
    val studio: String? = null
) : MediaMetadataData

data class ShowMetadata(
    override val title: String,
    override val posterPath: String? = null,
    override val durationSecs: Int? = null,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeName: String? = null
) : MediaMetadataData
