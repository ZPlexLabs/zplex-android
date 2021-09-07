package zechs.zplex.models.tvdb

data class Data(
    val added: String?,
    val airsDayOfWeek: String?,
    val airsTime: String?,
    val aliases: List<String>?,
    val poster: String?,
    val banner: String?,
    val fanart: String?,
    val firstAired: String?,
    val genre: List<String>?,
    val id: Int?,
    val imdbId: String?,
    val lastUpdated: Int?,
    val network: String?,
    val networkId: String?,
    val overview: String?,
    val rating: String?,
    val runtime: String?,
    val seriesId: String?,
    val seriesName: String?,
    val siteRating: Float?,
    val siteRatingCount: Int?,
    val slug: String?,
    val status: String?,
    val zap2itId: String?
)