package zechs.zplex.ui.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import zechs.zplex.data.model.entities.WatchedMovie
import zechs.zplex.data.model.entities.WatchedShow
import zechs.zplex.data.repository.WatchedRepository
import zechs.zplex.ui.player.MPVActivity.Companion.TAG
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val watchedRepository: WatchedRepository
) : ViewModel() {

    private val _startDuration = Channel<Long>(Channel.CONFLATED)
    val startDuration = _startDuration

    fun getWatch(tmdbId: Int, isTv: Boolean, seasonNumber: Int?, episodeNumber: Int?) =
        viewModelScope.launch(Dispatchers.IO) {
            if (tmdbId < 0) {
                Log.d(TAG, "Ignoring tmdbId $tmdbId")
                return@launch
            }
            if (isTv) {
                if (seasonNumber == -1 || episodeNumber == -1) {
                    Log.d(TAG, "Ignoring season $seasonNumber and episode $episodeNumber")
                }
                val lookUpWatched = watchedRepository.getWatchedShow(
                    tmdbId, seasonNumber!!, episodeNumber!!
                )
                if (lookUpWatched == null) {
                    Log.d(TAG, "Video not found in database")
                } else {
                    if (!lookUpWatched.hasFinished()) {
                        Log.d(TAG, "Starting at ${lookUpWatched.watchedDuration}")
                        _startDuration.send(lookUpWatched.watchedDuration)
                    }
                }
            } else {
                val lookUpWatched = watchedRepository.getWatchedMovie(tmdbId)
                if (lookUpWatched == null) {
                    Log.d(TAG, "Video not found in database")
                } else {
                    if (!lookUpWatched.hasFinished()) {
                        Log.d(TAG, "Starting at ${lookUpWatched.watchedDuration}")
                        _startDuration.send(lookUpWatched.watchedDuration)
                    }
                }
            }
        }

    fun upsertWatchedShow(
        tmdbId: Int,
        name: String,
        posterPath: String?,
        seasonNumber: Int,
        episodeNumber: Int,
        watchedDuration: Long,
        totalDuration: Long
    ) = viewModelScope.launch(Dispatchers.IO) {
        val lookUpWatched = watchedRepository.getWatchedShow(tmdbId, seasonNumber, episodeNumber)
        val watch = lookUpWatched?.copy(
            name = name,
            posterPath = posterPath,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            watchedDuration = watchedDuration,
            totalDuration = totalDuration,
            createdAt = Calendar.getInstance().timeInMillis
        ) ?: WatchedShow(
            tmdbId, name,
            "tv", posterPath,
            seasonNumber, episodeNumber,
            watchedDuration, totalDuration,
            Calendar.getInstance().timeInMillis
        )

        watchedRepository.upsertWatchedShow(watch)
    }

    fun upsertWatchedMovie(
        tmdbId: Int,
        name: String,
        posterPath: String?,
        watchedDuration: Long,
        totalDuration: Long,
    ) = viewModelScope.launch(Dispatchers.IO) {
        val lookUpWatched = watchedRepository.getWatchedMovie(tmdbId)
        val watch = lookUpWatched?.copy(
            name = name,
            posterPath = posterPath,
            watchedDuration = watchedDuration,
            totalDuration = totalDuration,
            createdAt = Calendar.getInstance().timeInMillis
        ) ?: WatchedMovie(
            tmdbId, name,
            "movie", posterPath,
            watchedDuration, totalDuration, Calendar.getInstance().timeInMillis
        )
        watchedRepository.upsertWatchedMovie(watch)
    }

}