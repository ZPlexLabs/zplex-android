package zechs.zplex.ui.fragment.media

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.models.tmdb.collection.CollectionsResponse
import zechs.zplex.models.tmdb.entities.Episode
import zechs.zplex.models.tmdb.media.MediaResponse
import zechs.zplex.models.tmdb.media.TvResponse
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.repository.ZPlexRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.ui.movieResponseTmdb
import zechs.zplex.ui.movieResponseZplex
import zechs.zplex.utils.Event
import zechs.zplex.utils.Resource
import java.io.IOException

class MediaViewModel(
    app: Application,
    private val tmdbRepository: TmdbRepository,
    private val zplexRepository: ZPlexRepository
) : BaseAndroidViewModel(app) {

    private val _movieZplex = MutableLiveData<Event<Resource<movieResponseZplex>>>()
    val movieZplex: LiveData<Event<Resource<movieResponseZplex>>>
        get() = _movieZplex

    private val _dominantColor = MutableLiveData(Event(6770852))
    val dominantColor: LiveData<Event<Int>>
        get() = _dominantColor

    fun saveShow(show: Show) = viewModelScope.launch {
        tmdbRepository.upsertShow(show)
    }

    fun deleteShow(show: Show) = viewModelScope.launch {
        tmdbRepository.deleteShow(show)
    }

    fun getShow(id: Int) = tmdbRepository.fetchShow(id)

    fun saveMovie(movie: Movie) = viewModelScope.launch {
        tmdbRepository.upsertMovie(movie)
    }

    fun deleteMovie(movie: Movie) = viewModelScope.launch {
        tmdbRepository.deleteMovie(movie)
    }

    fun getMovie(id: Int) = tmdbRepository.fetchMovie(id)

    fun setDominantColor(color: Int) {
        if (_dominantColor.value == Event(color)) return
        _dominantColor.value = Event(color)
    }

    fun zplexGetMovie(tmdbId: Int) = viewModelScope.launch {
        _movieZplex.postValue(Event(Resource.Loading()))
        try {
            if (hasInternetConnection()) {
                val zplexMovie = zplexRepository.getMovie(tmdbId)
                _movieZplex.postValue(Event(handleZPlexMovieResponse(zplexMovie)))
            } else {
                _movieZplex.postValue(Event(Resource.Error("No internet connection")))
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            println("zplexGetMovie :  Message=${t.message}")
            _movieZplex.postValue(
                Event(
                    Resource.Error(
                        if (t is IOException) {
                            "Network Failure"
                        } else t.message ?: "Something went wrong"
                    )
                )
            )
        }
    }

    private fun handleZPlexMovieResponse(
        response: Response<movieResponseZplex>
    ): Resource<movieResponseZplex> {
        if (response.isSuccessful) {
            return Resource.Success(response.body()!!)
        }
        return Resource.Error(response.message())
    }

    private val _mediaResponse = MutableLiveData<Event<Resource<MediaResponse>>>()
    val mediaResponse: LiveData<Event<Resource<MediaResponse>>>
        get() = _mediaResponse

    fun getMedia(tmdbId: Int, mediaType: String) = viewModelScope.launch {
        _mediaResponse.postValue(Event(Resource.Loading()))
        try {
            if (hasInternetConnection()) {
                if (mediaType == "movie") {
                    val response = tmdbRepository.getMovie(tmdbId)
                    response.body()?.belongs_to_collection?.let {
                        val collectionsResponse = tmdbRepository.getCollection(it.id)
                        _mediaResponse.postValue(
                            Event(
                                handleMovieResponseWithCollection(
                                    response,
                                    collectionsResponse
                                )
                            )
                        )
                    } ?: _mediaResponse.postValue(Event(handleMovieResponse(response)))
                } else {
                    val response = tmdbRepository.getShow(tmdbId)
                    _mediaResponse.postValue(Event(handleTvResponse(response)))
                }
            } else {
                _mediaResponse.postValue(Event(Resource.Error("No internet connection")))
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            println("getMediaFlow :  Message=${t.message}")
            _mediaResponse.postValue(
                Event(
                    Resource.Error(
                        if (t is IOException) {
                            "Network Failure"
                        } else t.message ?: "Something went wrong"
                    )
                )
            )
        }
    }

    private fun handleMovieResponseWithCollection(
        response: Response<movieResponseTmdb>,
        collectionsResponse: Response<CollectionsResponse>
    ): Resource<MediaResponse> {
        if (response.isSuccessful && collectionsResponse.isSuccessful) {
            if (response.body() != null && collectionsResponse.body() != null) {
                val resultResponse = response.body()!!
                val collectionsResult = collectionsResponse.body()!!

                val year = resultResponse.release_date?.let { firstAired ->
                    if (firstAired.isNotBlank()) {
                        firstAired.take(4).toInt()
                    } else 0
                } ?: 0

                val videosList = resultResponse.videos?.results?.filter { v ->
                    v.site == "YouTube"
                } ?: listOf()

                val mediaResponse = MediaResponse(
                    id = resultResponse.id,
                    imdb_id = resultResponse.imdb_id,
                    name = resultResponse.title,
                    overview = resultResponse.overview,
                    poster_path = resultResponse.poster_path,
                    backdrop_path = resultResponse.backdrop_path,
                    related_media = collectionsResult.parts,
                    seasons = listOf(),
                    cast = resultResponse.credits.cast?.toList() ?: listOf(),
                    recommendations = resultResponse.recommendations?.results?.toList() ?: listOf(),
                    videos = videosList,
                    vote_average = resultResponse.vote_average?.div(2),
                    runtime = resultResponse.runtime,
                    genres = resultResponse.genres,
                    year = year,
                    belongs_to_collection = resultResponse.belongs_to_collection,
                    last_episode_to_air = null
                )
                return Resource.Success(mediaResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleTvResponse(
        response: Response<TvResponse>
    ): Resource<MediaResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                val seasonList = resultResponse.seasons?.toList() ?: listOf()
                val videosList = resultResponse.videos?.results?.filter { v ->
                    v.site == "YouTube"
                } ?: listOf()

                val runtime = if (resultResponse.episode_run_time?.isNotEmpty() == true) {
                    resultResponse.episode_run_time[0]
                } else 0

                val lastEpisodeToAir = resultResponse.last_episode_to_air?.let { ep ->
                    Episode(
                        id = ep.id,
                        episode_number = ep.episode_number,
                        guest_stars = ep.guest_stars,
                        name = ep.name,
                        overview = ep.overview,
                        season_number = ep.season_number,
                        still_path = ep.still_path,
                        fileId = null,
                        fileName = null,
                        fileSize = null
                    )
                }
                val mediaResponse = MediaResponse(
                    id = resultResponse.id,
                    imdb_id = null,
                    name = resultResponse.name,
                    overview = resultResponse.overview,
                    poster_path = resultResponse.poster_path,
                    backdrop_path = resultResponse.backdrop_path,
                    related_media = listOf(),
                    seasons = seasonList,
                    cast = resultResponse.credits.cast?.toList() ?: listOf(),
                    recommendations = resultResponse.recommendations?.results?.toList() ?: listOf(),
                    videos = videosList,
                    vote_average = resultResponse.vote_average?.div(2),
                    runtime = runtime,
                    genres = resultResponse.genres,
                    year = resultResponse.first_air_date?.take(4)?.toInt(),
                    belongs_to_collection = null,
                    last_episode_to_air = lastEpisodeToAir
                )
                return Resource.Success(mediaResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleMovieResponse(
        response: Response<movieResponseTmdb>
    ): Resource<MediaResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                val videosList = resultResponse.videos?.results?.filter { v ->
                    v.site == "YouTube"
                } ?: listOf()

                val mediaResponse = MediaResponse(
                    id = resultResponse.id,
                    imdb_id = resultResponse.imdb_id,
                    name = resultResponse.title,
                    overview = resultResponse.overview,
                    poster_path = resultResponse.poster_path,
                    backdrop_path = resultResponse.backdrop_path,
                    related_media = listOf(),
                    seasons = listOf(),
                    cast = resultResponse.credits.cast?.toList() ?: listOf(),
                    recommendations = resultResponse.recommendations?.results?.toList() ?: listOf(),
                    videos = videosList,
                    vote_average = resultResponse.vote_average?.div(2),
                    runtime = resultResponse.runtime,
                    genres = resultResponse.genres,
                    year = resultResponse.release_date?.take(4)?.toInt(),
                    belongs_to_collection = resultResponse.belongs_to_collection,
                    last_episode_to_air = null
                )
                return Resource.Success(mediaResponse)
            }
        }
        return Resource.Error(response.message())
    }


    fun calcDominantColor(drawable: Drawable, onFinish: (Int) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        Palette.from(bmp).generate { palette ->
            palette?.let { p ->
                onFinish(p.vibrantSwatch?.rgb ?: p.dominantSwatch?.rgb ?: 6770852)
            }
        }
    }
}