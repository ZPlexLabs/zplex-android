package zechs.zplex.ui.fragment.episodes

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.ThisApp
import zechs.zplex.models.dataclass.ConstantsResult
import zechs.zplex.models.drive.DriveResponse
import zechs.zplex.models.drive.File
import zechs.zplex.models.tmdb.entities.Episode
import zechs.zplex.models.tmdb.season.SeasonResponse
import zechs.zplex.repository.FilesRepository
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.utils.Constants.CLIENT_ID
import zechs.zplex.utils.Constants.CLIENT_SECRET
import zechs.zplex.utils.Constants.REFRESH_TOKEN
import zechs.zplex.utils.Constants.SEASON_EPISODE_REGEX
import zechs.zplex.utils.Constants.TEMP_TOKEN
import zechs.zplex.utils.Constants.ZPLEX
import zechs.zplex.utils.Constants.ZPLEX_DRIVE_ID
import zechs.zplex.utils.Constants.ZPLEX_SHOWS_ID
import zechs.zplex.utils.Resource
import zechs.zplex.utils.SessionManager
import java.io.IOException
import java.text.DecimalFormat

class EpisodesViewModel(
    app: Application,
    private val filesRepository: FilesRepository,
    private val tmdbRepository: TmdbRepository
) : AndroidViewModel(app) {

    private val database = Firebase.firestore

    private val pageSize = 1000
    private val orderBy = "name"

    private val accessToken = SessionManager(
        getApplication<Application>().applicationContext
    ).fetchAuthToken()

    val season: MutableLiveData<Resource<SeasonResponse>> = MutableLiveData()

    fun getSeason(tvId: Int, seasonNumber: Int) =
        viewModelScope.launch {
            season.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    getCredentials(tvId, seasonNumber)
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

    private fun getCredentials(tvId: Int, seasonNumber: Int) {
        database.collection("constants")
            .document("eQvhagfVy6IgH2Hcx1Kk")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val constantsResult = documentSnapshot.toObject<ConstantsResult>()
                constantsResult?.let {
                    ZPLEX = it.zplex
                    ZPLEX_DRIVE_ID = it.zplex_drive_id
                    ZPLEX_SHOWS_ID = it.zplex_shows_id
                    CLIENT_ID = it.client_id
                    CLIENT_SECRET = it.client_secret
                    REFRESH_TOKEN = it.refresh_token
                    TEMP_TOKEN = it.temp_token
                    CoroutineScope(Dispatchers.IO).launch {
                        val tmdb = tmdbRepository.getSeason(tvId, seasonNumber)

                        val folder = filesRepository.getDriveFiles(
                            pageSize = 1,
                            if (accessToken == "") it.temp_token else accessToken,
                            pageToken = "",
                            searchQuery(tvId, it.zplex_shows_id),
                            orderBy = "modifiedTime desc"
                        )
                        if (folder.isSuccessful
                            && folder.body() != null
                            && folder.body()!!.files.isNotEmpty()
                        ) {
                            val folderId = folder.body()!!.files[0].id
                            val drive = filesRepository.getDriveFiles(
                                pageSize,
                                if (accessToken == "") it.temp_token else accessToken,
                                "", driveQuery(folderId, seasonNumber), orderBy
                            )
                            season.postValue(handleSeasonResponse(tmdb, drive))
                        } else {
                            season.postValue(handleEpisodesResponse(tmdb))
                        }
                    }
                }
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
                            val nameSplit = SEASON_EPISODE_REGEX.toRegex().find(
                                it.name
                            )?.destructured?.toList()
                            // val seasonCount = nameSplit?.get(0)?.toInt() ?: 0
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

    private fun searchQuery(
        tmdbId: Int, showId: String
    ) = "name contains '${tmdbId}' and parents in '${showId}' and trashed = false"

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