package zechs.zplex.ui.media.adapter

import androidx.annotation.Keep
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.entities.Show
import zechs.zplex.data.model.entities.WatchedMovie
import zechs.zplex.data.model.tmdb.entities.Cast
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.data.model.tmdb.entities.Season
import zechs.zplex.data.model.tmdb.entities.Video

sealed class MediaDataModel {

    @Keep
    data class Header(
        val backdropPath: String?,
        val posterPath: String?,
        val rating: Double,
        val isImdbRating: Boolean,
        val genre: String,
        val runtime: String
    ) : MediaDataModel()

    @Keep
    data class Title(
        val title: String,
        val plot: String
    ) : MediaDataModel()

    @Keep
    data class LatestSeason(
        val showTmdbId: Int,
        val showName: String,
        val showPoster: String?,
        val seasonName: String,
        val seasonPosterPath: String?,
        val seasonNumber: Int,
        val seasonPlot: String,
        val seasonYearAndEpisodeCount: String,
    ) : MediaDataModel()

    @Keep
    data class PartOfCollection(
        val bannerPoster: String?,
        val collectionName: String,
        val collectionId: Int
    ) : MediaDataModel()

    @Keep
    data class ShowButton(
        val seasons: List<Season>,
        val show: Show
    ) : MediaDataModel()

    @Keep
    data class MovieButton(
        val movie: Movie,
        val watchedMovie: WatchedMovie?,
        val year: Int?
    ) : MediaDataModel()

    @Keep
    data class Casts(
        val heading: String,
        val casts: List<Cast>
    ) : MediaDataModel()

    @Keep
    data class Recommendations(
        val heading: String,
        val recommendations: List<Media>
    ) : MediaDataModel()

    @Keep
    data class MoreFromCompany(
        val heading: String,
        val more: List<Media>
    ) : MediaDataModel()

    @Keep
    data class Videos(
        val heading: String,
        val videos: List<Video>
    ) : MediaDataModel()

}