package zechs.zplex.ui.activity.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import zechs.zplex.models.dataclass.WatchedMovie
import zechs.zplex.models.dataclass.WatchedShow
import zechs.zplex.repository.WatchedRepository

class PlayerViewModel(
    private val watchedRepository: WatchedRepository
) : ViewModel() {

    fun upsertWatchedShow(
        tmdbId: Int,
        name: String,
        posterPath: String?,
        seasonNumber: Int,
        episodeNumber: Int,
        watchedDuration: Long,
        totalDuration: Long,
        isLastEpisode: Boolean
    ) = viewModelScope.launch {
        val lookUpWatched = watchedRepository.getWatchedShow(tmdbId)
        val watch = lookUpWatched?.copy(
            name = name,
            posterPath = posterPath,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            watchedDuration = watchedDuration,
            totalDuration = totalDuration
        ) ?: WatchedShow(
            tmdbId, name,
            "tv", posterPath,
            seasonNumber, episodeNumber,
            watchedDuration, totalDuration
        )

        if (watch.hasFinished() && isLastEpisode) {
            watchedRepository.deleteWatchedShow(watch)
        } else watchedRepository.upsertWatchedShow(watch)
    }

    fun upsertWatchedMovie(
        tmdbId: Int,
        name: String,
        posterPath: String?,
        watchedDuration: Long,
        totalDuration: Long,
    ) = viewModelScope.launch {
        val lookUpWatched = watchedRepository.getWatchedMovie(tmdbId)
        val watch = lookUpWatched?.copy(
            name = name,
            posterPath = posterPath,
            watchedDuration = watchedDuration,
            totalDuration = totalDuration
        ) ?: WatchedMovie(
            tmdbId, name,
            "movie", posterPath,
            watchedDuration, totalDuration
        )

        if (watch.hasFinished()) {
            watchedRepository.deleteWatchedMovie(watch)
        } else watchedRepository.upsertWatchedMovie(watch)
    }

}