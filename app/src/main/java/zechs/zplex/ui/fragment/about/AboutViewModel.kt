package zechs.zplex.ui.fragment.about


import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.ThisApp
import zechs.zplex.models.drive.DriveResponse
import zechs.zplex.models.drive.File
import zechs.zplex.models.tmdb.credits.CreditsResponse
import zechs.zplex.models.tmdb.movies.MoviesResponse
import zechs.zplex.models.tvdb.actors.ActorsResponse
import zechs.zplex.models.tvdb.series.SeriesResponse
import zechs.zplex.repository.FilesRepository
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.repository.TvdbRepository
import zechs.zplex.utils.Constants.TEMP_TOKEN
import zechs.zplex.utils.Resource
import zechs.zplex.utils.SessionManager
import java.io.IOException


class AboutViewModel(
    app: Application,
    private val filesRepository: FilesRepository,
    private val tvdbRepository: TvdbRepository,
    private val tmdbRepository: TmdbRepository
) : AndroidViewModel(app) {

    // TVDB
    val series: MutableLiveData<Resource<SeriesResponse>> = MutableLiveData()
    val actors: MutableLiveData<Resource<ActorsResponse>> = MutableLiveData()

    //TMDB
    val movies: MutableLiveData<Resource<MoviesResponse>> = MutableLiveData()
    val credits: MutableLiveData<Resource<CreditsResponse>> = MutableLiveData()

    val mediaList: MutableLiveData<Resource<DriveResponse>> = MutableLiveData()

    private val accessToken = SessionManager(
        getApplication<Application>().applicationContext
    ).fetchAuthToken()

    private val pageSize = 1000
    private val orderBy = "name"

    fun saveShow(file: File) = viewModelScope.launch {
        filesRepository.upsert(file)
    }

    fun deleteShow(file: File) = viewModelScope.launch {
        filesRepository.deleteFile(file)
    }

    fun getShow(id: String) = filesRepository.getFile(id)

    fun getMediaFiles(driveQuery: String) =
        viewModelScope.launch {
            mediaList.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = filesRepository.getDriveFiles(
                        pageSize,
                        if (accessToken == "") TEMP_TOKEN else accessToken,
                        "", driveQuery, orderBy
                    )
                    mediaList.postValue(handleMediaListResponse(response))
                } else {
                    mediaList.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                if (t is IOException) mediaList.postValue(Resource.Error("Network Failure"))
                else mediaList.postValue(Resource.Error(t.message ?: "Something went wrong"))
            }
        }

    private fun handleMediaListResponse(
        response: Response<DriveResponse>
    ): Resource<DriveResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    // TVDB
    fun getSeries(seriesId: Int) =
        viewModelScope.launch {
            series.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = tvdbRepository.getSeries(seriesId)
                    series.postValue(handleSeriesListResponse(response))
                } else {
                    series.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)

                if (t is IOException) {
                    series.postValue(Resource.Error("Network Failure"))
                } else {
                    series.postValue(
                        Resource.Error(t.message ?: "Something went wrong")
                    )
                }
            }
        }

    private fun handleSeriesListResponse(
        response: Response<SeriesResponse>
    ): Resource<SeriesResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun getActor(seriesId: Int) =
        viewModelScope.launch {
            actors.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = tvdbRepository.getActors(seriesId)
                    actors.postValue(handleActorsListResponse(response))
                } else {
                    actors.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)

                if (t is IOException) {
                    actors.postValue(Resource.Error("Network Failure"))
                } else {
                    actors.postValue(
                        Resource.Error(t.message ?: "Something went wrong")
                    )
                }
            }
        }

    private fun handleActorsListResponse(response: Response<ActorsResponse>): Resource<ActorsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    // TMDB

    fun getMovies(moviesId: Int) =
        viewModelScope.launch {
            movies.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = tmdbRepository.getMovies(moviesId)
                    movies.postValue(handleMoviesListResponse(response))
                } else {
                    movies.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)

                if (t is IOException) {
                    movies.postValue(Resource.Error("Network Failure"))
                } else {
                    movies.postValue(
                        Resource.Error(t.message ?: "Something went wrong")
                    )
                }
            }
        }

    private fun handleMoviesListResponse(
        response: Response<MoviesResponse>
    ): Resource<MoviesResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun getCredits(moviesId: Int) =
        viewModelScope.launch {
            credits.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = tmdbRepository.getCredits(moviesId)
                    credits.postValue(handleCreditsListResponse(response))
                } else {
                    credits.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)

                if (t is IOException) {
                    credits.postValue(Resource.Error("Network Failure"))
                } else {
                    credits.postValue(
                        Resource.Error(t.message ?: "Something went wrong")
                    )
                }
            }
        }

    private fun handleCreditsListResponse(
        response: Response<CreditsResponse>
    ): Resource<CreditsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<ThisApp>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}