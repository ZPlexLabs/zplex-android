package zechs.zplex.ui.home.adapter

import androidx.annotation.Keep
import zechs.zplex.ui.home.adapter.watched.WatchedDataModel
import zechs.zplex.zplex_api.data.remote.api.movies.LatestMovie
import zechs.zplex.zplex_api.data.remote.api.suggestions.SuggestionMediaItem
import zechs.zplex.zplex_api.data.remote.api.tvshows.LatestTvShow

typealias tmdbMedia = zechs.zplex.data.model.tmdb.entities.Media

enum class MenuType {
    TV_SHOWS, MOVIES
}

sealed class HomeDataModel {

    @Keep
    data class Header(
        val heading: String
    ) : HomeDataModel()

    @Keep
    data class MediaMenu(
        val banner: String,
        val title: String,
        val type: MenuType
    ) : HomeDataModel()

    @Keep
    data class LatestTvShows(
        val show: List<LatestTvShow>
    ) : HomeDataModel()

    @Keep
    data class LatestMovies(
        val movies: List<LatestMovie>
    ) : HomeDataModel()

    @Keep
    data class Suggestions(
        val suggestions: List<SuggestionMediaItem>
    ) : HomeDataModel()

    @Keep
    data class Watched(
        val watched: List<WatchedDataModel>
    ) : HomeDataModel()

}