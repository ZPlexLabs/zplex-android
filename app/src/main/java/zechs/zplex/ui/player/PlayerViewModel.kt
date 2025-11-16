package zechs.zplex.ui.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import zechs.zplex.data.model.entities.WatchedMovie
import zechs.zplex.data.model.entities.WatchedShow
import zechs.zplex.data.model.tmdb.entities.Episode
import zechs.zplex.data.repository.DriveRepository
import zechs.zplex.data.repository.WatchedRepository
import zechs.zplex.ui.player.MPVActivity.Companion.TAG
import zechs.zplex.utils.SessionManager
import zechs.zplex.utils.state.Resource
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val watchedRepository: WatchedRepository,
    private val sessionManager: SessionManager,
    private val driveRepository: DriveRepository,
    private val gson: Gson,
) : ViewModel() {

    private val _startDuration = Channel<Long>(Channel.CONFLATED)
    val startDuration = _startDuration.receiveAsFlow()

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
                    _startDuration.send(0)
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
                    _startDuration.send(0)
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
        val title = "$name - S${seasonNumber}E${episodeNumber}"
        Log.d(TAG, "Saving progress: $title at $watchedDuration/$totalDuration")
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
        Log.d(TAG, "Saving progress: $name at $watchedDuration/$totalDuration")
    }


    data class Playback(
        val item: PlaybackItem?,
        val token: String
    )

    var head: PlaybackItem? = null
        private set
    private val _current = Channel<Resource<Playback?>>(Channel.CONFLATED)
    val current = _current.receiveAsFlow()

    private var playlistJson: String? = null

    // for side sheet
    fun play(newFileId: String) {
        if (head?.fileId == newFileId) return
        setPlaylist(playlistJson!!, findIndexFromFileId(newFileId))
    }

    fun findIndexFromFileId(newFileId: String): Int {
        return getPlaylist().indexOfFirst { pair -> pair.first.fileId == newFileId }
    }

    // here boolean is for currentlyPlaying
    fun getPlaylist(): List<Pair<Episode, Boolean>> {
        if (playlistJson.isNullOrEmpty()) return emptyList()
        val playbackItemType = object : TypeToken<List<GsonPlaybackItem>>() {}.type
        return gson.fromJson<List<GsonPlaybackItem>>(playlistJson, playbackItemType)
            .mapNotNull { playbackItem ->
                val temp = playbackItem.toPlaybackItem()
                if (temp is Show) {
                    val currentlyPlaying = head?.fileId == temp.fileId
                    Pair(temp.episode, currentlyPlaying)
                } else null
            }.distinctBy { it.first.fileId }
    }


    fun setPlaylist(playlist: String, startIndex: Int) {
        playlistJson = playlist
        val playbackItemType = object : TypeToken<List<GsonPlaybackItem>?>() {}.type
        gson.fromJson<List<GsonPlaybackItem>>(playlist, playbackItemType)
            ?.map { it.toPlaybackItem() }
            ?.let { playlistList ->
                setPlaylist(playlistList.toList().distinctBy { it.fileId }, startIndex)
            }
    }

    private fun setPlaylist(playlist: List<PlaybackItem>, startIndex: Int = 0) {
        if (playlist.isEmpty()) {
            head = null
            updateWithToken()
        } else {
            for (i in 0 until playlist.size - 1) {
                playlist[i].next = playlist[i + 1]
                playlist[i + 1].prev = playlist[i]
            }
            head = playlist[startIndex]
            updateWithToken()
        }
    }

    fun next() {
        if (head?.next == null) return
        head = head?.next
        updateWithToken()
    }

    fun previous() {
        if (head?.prev == null) return
        head = head?.prev
        updateWithToken()
    }

    private fun updateWithToken() = viewModelScope.launch(Dispatchers.IO) {
        if (head != null) {
            if (head!!.offline) {
                Log.d(TAG, "Offline : ${head!!.title} (${head!!.fileId})")
                _current.send(Resource.Success(Playback(head, "")))
                return@launch
            }
        }

        val client = sessionManager.fetchClient() ?: run {
            _current.send(Resource.Error("Client not found"))
            return@launch
        }
        when (val tokenResponse = driveRepository.fetchAccessToken(client)) {
            is Resource.Success -> {
                _current.send(Resource.Success(Playback(head, tokenResponse.data!!.accessToken)))
            }

            is Resource.Error -> {
                _current.send(Resource.Success(Playback(head, tokenResponse.message!!)))
            }

            else -> {}
        }
    }

    fun saveProgress(
        current: PlaybackItem?,
        watchedDuration: Long,
        totalDuration: Long
    ) = viewModelScope.launch(Dispatchers.IO) {
        val playbackItem = current ?: run {
            Log.d(TAG, "No playback item found, not saving progress")
            return@launch
        }
        if (playbackItem is Movie) {
            upsertWatchedMovie(
                playbackItem.tmdbId, playbackItem.title, playbackItem.posterPath,
                watchedDuration, totalDuration
            )
        } else if (playbackItem is Show) {
            upsertWatchedShow(
                playbackItem.tmdbId, playbackItem.title, playbackItem.posterPath,
                playbackItem.seasonNumber, playbackItem.episodeNumber,
                watchedDuration, totalDuration
            )
        }
    }

}