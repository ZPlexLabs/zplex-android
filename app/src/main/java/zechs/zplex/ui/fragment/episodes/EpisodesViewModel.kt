package zechs.zplex.ui.fragment.episodes

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.ThisApp
import zechs.zplex.models.drive.DriveResponse
import zechs.zplex.models.drive.File
import zechs.zplex.models.tmdb.entities.Episode
import zechs.zplex.models.tmdb.season.SeasonResponse
import zechs.zplex.repository.FilesRepository
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.utils.Constants.TEMP_TOKEN
import zechs.zplex.utils.Constants.regexFile
import zechs.zplex.utils.Resource
import zechs.zplex.utils.SessionManager
import java.io.IOException
import java.text.DecimalFormat

class EpisodesViewModel(
    app: Application,
    private val filesRepository: FilesRepository,
    private val tmdbRepository: TmdbRepository
) : AndroidViewModel(app) {

    private val pageSize = 1000
    private val orderBy = "name"

    private val accessToken = SessionManager(
        getApplication<Application>().applicationContext
    ).fetchAuthToken()

    //    val searchList: MutableLiveData<Resource<DriveResponse>> = MutableLiveData()
    val season: MutableLiveData<Resource<SeasonResponse>> = MutableLiveData()

//    fun getSeason(tvId: Int, seasonNumber: Int) =
//        viewModelScope.launch {
//            season.postValue(Resource.Loading())
//            try {
//                if (hasInternetConnection()) {
//                    val tmdb = tmdbRepository.getSeason(tvId, seasonNumber)
//                    season.postValue(handleEpisodesResponse(tmdb))
//                } else {
//                    season.postValue(Resource.Error("No internet connection"))
//                }
//            } catch (t: Throwable) {
//                println(t.stackTrace)
//                println(t.message)
//                season.postValue(
//                    Resource.Error(
//                        if (t is IOException) {
//                            "Network Failure"
//                        } else t.message ?: "Something went wrong"
//                    )
//                )
//            }
//        }
//
//    private fun handleEpisodesResponse(
//        response: Response<SeasonResponse>
//    ): Resource<SeasonResponse> {
//        if (response.isSuccessful) {
//            response.body()?.let { resultResponse ->
//                return Resource.Success(resultResponse)
//            }
//        }
//        return Resource.Error(response.message())
//    }
//
//    fun doSearchFor(searchQuery: String) =
//        viewModelScope.launch {
//            searchList.postValue(Resource.Loading())
//            try {
//                if (hasInternetConnection()) {
//                    val response = filesRepository.getDriveFiles(
//                        pageSize = 1000,
//                        if (accessToken == "") TEMP_TOKEN else accessToken,
//                        pageToken = "", searchQuery, orderBy = "name asc"
//                    )
//                    searchList.postValue(handleSearchListResponse(response))
//                } else {
//                    searchList.postValue(Resource.Error("No internet connection"))
//                }
//            } catch (t: Throwable) {
//                println(t.stackTrace)
//                println(t.message)
//                searchList.postValue(
//                    Resource.Error(
//                        if (t is IOException) {
//                            "Network Failure"
//                        } else t.message ?: "Something went wrong"
//                    )
//                )
//            }
//        }
//
//    private fun handleSearchListResponse(
//        response: Response<DriveResponse>
//    ): Resource<DriveResponse> {
//        if (response.isSuccessful) {
//            response.body()?.let { resultResponse ->
//                return Resource.Success(resultResponse)
//            }
//        }
//        return Resource.Error(response.message())
//    }


    fun getSeason(tvId: Int, seasonNumber: Int, driveId: String?) =
        viewModelScope.launch {
            season.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val tmdb = tmdbRepository.getSeason(tvId, seasonNumber)
                    println("driveId=$driveId")
                    if (driveId == null) {
                        season.postValue(handleEpisodesResponse(tmdb))
                    } else {
                        val drive = filesRepository.getDriveFiles(
                            pageSize,
                            if (accessToken == "") TEMP_TOKEN else accessToken,
                            "", driveQuery(driveId, seasonNumber), orderBy
                        )
                        season.postValue(handleSeasonResponse(tmdb, drive))
                    }
                } else {
                    season.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)
                season.postValue(
                    Resource.Error(
                        if (t is IOException) {
                            "Network Failure"
                        } else t.message ?: "Something went wrong"
                    )
                )
            }
        }

    private fun handleEpisodesResponse(
        tmdb: Response<SeasonResponse>
    ): Resource<SeasonResponse> {
        if (tmdb.isSuccessful) {
            if (tmdb.body() != null) {
                val tmdbResponse = tmdb.body()
                if (tmdbResponse?.episodes != null) {
                    val result = tmdbResponse.episodes.map { ep ->
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

                    val resultResponse = SeasonResponse(
                        id = null,
                        episodes = result.toMutableList(),
                        name = tmdbResponse.name,
                        overview = tmdbResponse.overview,
                        poster_path = tmdbResponse.poster_path,
                        season_number = tmdbResponse.season_number
                    )
                    return Resource.Success(resultResponse)
                }
            }
        }
        return Resource.Error(tmdb.message())
    }

    private fun handleSeasonResponse(
        tmdb: Response<SeasonResponse>,
        drive: Response<DriveResponse>
    ): Resource<SeasonResponse> {
        if (tmdb.isSuccessful && drive.isSuccessful) {
            if (tmdb.body() != null && drive.body() != null) {
                val tmdbResponse = tmdb.body()
                val driveResponse = drive.body()
                if (tmdbResponse?.episodes != null && driveResponse?.files != null) {

                    driveResponse.files.let { files ->
                        val filesById: Map<Int, File> = files.associateBy {
                            val nameSplit = regexFile.toRegex().find(
                                it.name
                            )?.destructured?.toList()
                            val episodeCount = nameSplit?.get(1)?.toInt() ?: 0
                            episodeCount
                        }

                        val result = tmdbResponse.episodes.map { ep ->
                            val file = filesById[ep.episode_number]
                            Episode(
                                id = ep.id,
                                episode_number = ep.episode_number,
                                guest_stars = ep.guest_stars,
                                name = ep.name,
                                overview = ep.overview,
                                season_number = ep.season_number,
                                still_path = ep.still_path,
                                fileId = file?.id,
                                fileName = file?.name,
                                fileSize = file?.humanSize
                            )
                        }

                        val resultResponse = SeasonResponse(
                            id = null,
                            episodes = result,
                            name = tmdbResponse.name,
                            overview = tmdbResponse.overview,
                            poster_path = tmdbResponse.poster_path,
                            season_number = tmdbResponse.season_number
                        )
                        return Resource.Success(resultResponse)
                    }
                }
            }

        }
        return Resource.Error(tmdb.message())
    }


    private fun driveQuery(
        driveId: String, seasonNumber: Int
    ): String {
        val twoPlace = DecimalFormat("00")
        return "name contains 'S${
            twoPlace.format(seasonNumber)
        }' and name contains 'mkv' and '${
            driveId
        }' in parents and trashed = false"
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<ThisApp>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}