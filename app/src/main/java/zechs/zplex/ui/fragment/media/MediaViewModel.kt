package zechs.zplex.ui.fragment.media

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.*
import androidx.palette.graphics.Palette
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.ThisApp
import zechs.zplex.models.dataclass.ConstantsResult
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.models.drive.DriveResponse
import zechs.zplex.models.tmdb.collection.CollectionsResponse
import zechs.zplex.models.tmdb.entities.Episode
import zechs.zplex.models.tmdb.media.MediaResponse
import zechs.zplex.models.tmdb.media.MovieResponse
import zechs.zplex.models.tmdb.media.TvResponse
import zechs.zplex.models.witch.DashVideoResponseItem
import zechs.zplex.models.witch.MessageResponse
import zechs.zplex.repository.FilesRepository
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.repository.WitchRepository
import zechs.zplex.utils.Constants.CLIENT_ID
import zechs.zplex.utils.Constants.CLIENT_SECRET
import zechs.zplex.utils.Constants.DOCUMENT_PATH
import zechs.zplex.utils.Constants.REFRESH_TOKEN
import zechs.zplex.utils.Constants.TEMP_TOKEN
import zechs.zplex.utils.Constants.ZPLEX
import zechs.zplex.utils.Constants.ZPLEX_DRIVE_ID
import zechs.zplex.utils.Constants.ZPLEX_MOVIES_ID
import zechs.zplex.utils.Constants.ZPLEX_SHOWS_ID
import zechs.zplex.utils.Event
import zechs.zplex.utils.Resource
import zechs.zplex.utils.SessionManager
import java.io.IOException

class MediaViewModel(
    app: Application,
    private val filesRepository: FilesRepository,
    private val tmdbRepository: TmdbRepository,
    private val witchRepository: WitchRepository
) : AndroidViewModel(app) {

    private val database = Firebase.firestore

    private val accessToken = SessionManager(
        getApplication<Application>().applicationContext
    ).fetchAuthToken()

    private val _searchList = MutableLiveData<Event<Resource<DriveResponse>>>()
    val searchList: LiveData<Event<Resource<DriveResponse>>>
        get() = _searchList

    val media: MutableLiveData<Resource<MediaResponse>> = MutableLiveData()

    private val _dominantColor = MutableLiveData(Event(6770852))
    val dominantColor: LiveData<Event<Int>>
        get() = _dominantColor

    private val _witchMessage = MutableLiveData<Event<String>>()
    val witchMessage: LiveData<Event<String>>
        get() = _witchMessage

    private val _dashVideo = MutableLiveData<Event<Resource<List<DashVideoResponseItem>?>>>()
    val dashVideo: LiveData<Event<Resource<List<DashVideoResponseItem>?>>>
        get() = _dashVideo

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

    fun getMediaFlow(tmdbId: Int, mediaType: String) = channelFlow {
        send(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                if (mediaType == "movie") {
                    val response = tmdbRepository.getMovie(tmdbId)
                    if (response.body()?.belongs_to_collection != null) {
                        val collectionsResponse = tmdbRepository.getCollection(
                            collectionId = response.body()?.belongs_to_collection!!.id
                        )
                        send(
                            handleMovieResponseWithCollection(
                                response, collectionsResponse
                            )
                        )
                    } else {
                        send(handleMovieResponse(response))
                    }
                } else {
                    val response = tmdbRepository.getShow(tmdbId)
                    send(handleTvResponse(response))
                }
            } else {
                send(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            println("getMediaFlow :  Message=${t.message}")
            send(
                Resource.Error(
                    if (t is IOException) "Network Failure" else t.message ?: "Something went wrong"
                )
            )
        }
    }.asLiveData()

    private fun handleMovieResponseWithCollection(
        response: Response<MovieResponse>,
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
        response: Response<MovieResponse>
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

    private fun getCredentials(tmdbId: Int) {
        database.collection("constants")
            .document(DOCUMENT_PATH)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val constantsResult = documentSnapshot.toObject<ConstantsResult>()
                constantsResult?.let {
                    ZPLEX = it.zplex
                    ZPLEX_DRIVE_ID = it.zplex_drive_id
                    ZPLEX_MOVIES_ID = it.zplex_movies_id
                    ZPLEX_SHOWS_ID = it.zplex_shows_id
                    CLIENT_ID = it.client_id
                    CLIENT_SECRET = it.client_secret
                    REFRESH_TOKEN = it.refresh_token
                    TEMP_TOKEN = it.temp_token
                    CoroutineScope(Dispatchers.IO).launch {
                        val response = filesRepository.getDriveFiles(
                            pageSize = 1,
                            if (accessToken == "") it.temp_token else accessToken,
                            pageToken = null,
                            driveQuery = searchQuery(tmdbId, it.zplex_movies_id),
                            orderBy = "modifiedTime desc"
                        )
                        _searchList.postValue(Event(handleSearchListResponse(response)))
                    }
                }
            }
    }

    fun doSearchFor(tmdbId: Int) = viewModelScope.launch {
        _searchList.postValue(Event(Resource.Loading()))
        try {
            if (hasInternetConnection()) {
                getCredentials(tmdbId)
            } else {
                _searchList.postValue(Event(Resource.Error("No internet connection")))
            }
        } catch (t: Throwable) {
            println(t.stackTrace)
            println(t.message)
            _searchList.postValue(
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


    private fun handleSearchListResponse(
        response: Response<DriveResponse>
    ): Resource<DriveResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun getDashVideos(fileId: String) = viewModelScope.launch {
        try {
            if (hasInternetConnection()) {
                val response = witchRepository.getDashVideos(fileId)
                _dashVideo.value = Event(handleDashVideoResponse(response))
            } else {
                _dashVideo.value = Event(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            println(t)
            println(t.message)
            _dashVideo.value = Event(
                Resource.Error(
                    if (t is IOException) {
                        "Network Failure"
                    } else t.message ?: "Something went wrong"
                )
            )
        }
    }

    private fun handleDashVideoResponse(
        response: Response<List<DashVideoResponseItem>?>
    ): Resource<List<DashVideoResponseItem>?> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun requestMovie(imdbId: String, tmdbId: String, deviceId: String) = viewModelScope.launch {

        try {
            if (hasInternetConnection()) {
                val response = witchRepository.requestMovie(
                    imdbId, tmdbId, deviceId
                )
                _witchMessage.value = Event(handleWitchResponse(response))
            } else {
                _witchMessage.value = Event("No internet connection")
            }
        } catch (t: Throwable) {
            println(t)
            println(t.message)
            _witchMessage.value = Event(
                if (t is IOException) {
                    "Network Failure"
                } else t.message ?: "Something went wrong"
            )
        }
    }

    private fun handleWitchResponse(
        response: Response<MessageResponse>
    ): String {
        if (response.isSuccessful) {
            return response.body()?.message ?: "Something went wrong"
        }
        return response.message()
    }

    private fun searchQuery(
        tmdbId: Int, movieId: String
    ) = "name contains '${tmdbId}' and '${movieId}' in parents and trashed = false"

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<ThisApp>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
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