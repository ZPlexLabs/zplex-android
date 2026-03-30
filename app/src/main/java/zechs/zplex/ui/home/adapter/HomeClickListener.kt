package zechs.zplex.ui.home.adapter

import zechs.zplex.zplex_api.data.remote.api.movies.LatestMovie
import zechs.zplex.zplex_api.data.remote.api.suggestions.SuggestionMediaItem
import zechs.zplex.zplex_api.data.remote.api.tvshows.LatestTvShow

interface HomeClickListener {

    fun onClickMenu(type: MenuType)
    fun onClickLatestShow(show: LatestTvShow)
    fun onClickLatestMovie(movie: LatestMovie)
    fun onClickSuggestionItem(suggestion: SuggestionMediaItem)

}