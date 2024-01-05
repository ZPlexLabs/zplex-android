package zechs.zplex.ui.episodes

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.data.model.drive.DriveFile
import zechs.zplex.data.model.drive.File
import zechs.zplex.data.model.tmdb.entities.Episode
import zechs.zplex.data.repository.DriveRepository
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.ui.episodes.EpisodesFragment.Companion.TAG
import zechs.zplex.ui.episodes.adapter.EpisodesDataModel
import zechs.zplex.utils.SessionManager
import zechs.zplex.utils.state.Event
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.state.ResourceExt.Companion.postError
import zechs.zplex.utils.util.DriveApiQueryBuilder
import javax.inject.Inject

typealias seasonResponseTmdb = zechs.zplex.data.model.tmdb.season.SeasonResponse

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    app: Application,
    private val tmdbRepository: TmdbRepository,
    private val driveRepository: DriveRepository,
    private val sessionManager: SessionManager
) : BaseAndroidViewModel(app) {

    var hasLoggedIn = false
        private set

    fun updateStatus() = viewModelScope.launch {
        hasLoggedIn = getLoginStatus()
    }

    private suspend fun getLoginStatus(): Boolean {
        sessionManager.fetchClient() ?: return false
        sessionManager.fetchRefreshToken() ?: return false
        return true
    }


    private val _seasonResponse = MutableLiveData<Resource<List<EpisodesDataModel>>>()
    val episodesResponse: LiveData<Resource<List<EpisodesDataModel>>>
        get() = _seasonResponse

    fun getSeason(
        tmdbId: Int,
        seasonNumber: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        _seasonResponse.postValue((Resource.Loading()))
        try {
            if (hasInternetConnection()) {
                val tmdbSeason = tmdbRepository.getSeason(tmdbId, seasonNumber)
                _seasonResponse.postValue((handleSeasonResponse(tmdbId, tmdbSeason)))
            } else {
                _seasonResponse.postValue((Resource.Error("No internet connection")))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _seasonResponse.postValue(postError(e))
        }
    }

    private suspend fun handleSeasonResponse(
        tmdbId: Int,
        response: Response<seasonResponseTmdb>
    ): Resource<List<EpisodesDataModel>> {
        if (response.body() != null) {
            val result = response.body()!!
            val seasonDataModel = mutableListOf<EpisodesDataModel>()

            seasonDataModel.add(createSeasonHeader(result))

            if (!result.episodes.isNullOrEmpty()) {
                val savedShow = tmdbRepository.fetchShowById(tmdbId)
                if (savedShow?.fileId != null) {
                    handleSeasonFolder(result, savedShow.fileId, seasonDataModel)
                } else {
                    handleDefaultMapping(result.episodes, seasonDataModel)
                }
            }

            return Resource.Success(seasonDataModel.toList())
        }
        return Resource.Error(response.message())
    }

    private fun createSeasonHeader(result: seasonResponseTmdb): EpisodesDataModel.Header {
        return EpisodesDataModel.Header(
            seasonNumber = "Season ${result.season_number}",
            seasonName = result.name,
            seasonPosterPath = result.poster_path,
            seasonOverview = result.overview ?: "No description"
        )
    }

    private suspend fun handleSeasonFolder(
        result: seasonResponseTmdb,
        showFolderId: String,
        seasonDataModel: MutableList<EpisodesDataModel>
    ) {
        val seasonFolderName = "Season ${result.season_number}"
        val seasonFolder = findSeasonFolder(showFolderId, seasonFolderName)

        if (seasonFolder != null) {
            handleEpisodesInFolder(result.episodes!!, seasonFolder.id, seasonDataModel)
        } else {
            Log.d(TAG, "No folder found with name \"$seasonFolderName\"")
            handleDefaultMapping(result.episodes!!, seasonDataModel)
        }
    }

    private suspend fun findSeasonFolder(
        showFolderId: String,
        seasonFolderName: String
    ): DriveFile? {
        val filesInShowFolder = driveRepository.getAllFilesInFolder(
            queryBuilder = DriveApiQueryBuilder()
                .inParents(showFolderId)
                .mimeTypeEquals("application/vnd.google-apps.folder")
                .trashed(false)
        )

        if (filesInShowFolder is Resource.Success && filesInShowFolder.data != null) {
            return filesInShowFolder.data
                .firstOrNull { it.name.equals(seasonFolderName, true) }
                ?.toDriveFile()
        }
        return null
    }

    private suspend fun handleEpisodesInFolder(
        episodes: List<Episode>,
        seasonFolderId: String,
        seasonDataModel: MutableList<EpisodesDataModel>
    ) {
        val episodesInFolder = driveRepository.getAllFilesInFolder(
            queryBuilder = DriveApiQueryBuilder()
                .inParents(seasonFolderId)
                .mimeTypeNotEquals("application/vnd.google-apps.folder")
                .trashed(false)
        )

        if (episodesInFolder is Resource.Success && episodesInFolder.data != null) {
            processMatchingEpisodes(episodes, episodesInFolder.data, seasonDataModel)
        } else {
            Log.d(TAG, "No files found in season folder")
            handleDefaultMapping(episodes, seasonDataModel)
        }
    }

    private fun processMatchingEpisodes(
        episodes: List<Episode>,
        filesInFolder: List<File>,
        seasonDataModel: MutableList<EpisodesDataModel>
    ) {
        val episodeMap =
            buildEpisodeMap(filesInFolder.map { it.toDriveFile() }.filter { it.isVideoFile })

        var match = 0
        episodes.forEach { episode ->
            val matchingEpisode = findMatchingEpisode(episode, episodeMap)
            if (matchingEpisode == null) {
                Log.d(TAG, "No matching file found for episode ${getEpisodePattern(episode)}")
                seasonDataModel.add(createEpisodeModel(episode, fileId = null))
            } else {
                Log.d(TAG, "Found matching file for episode ${getEpisodePattern(episode)}")
                seasonDataModel.add(createEpisodeModel(episode, fileId = matchingEpisode.id))
                match++
            }
        }
        Log.d(TAG, "Matched $match out of ${episodes.size} episodes")
    }


    private fun buildEpisodeMap(foundEpisodes: List<DriveFile>): Map<String, DriveFile> {
        val episodeMap = mutableMapOf<String, DriveFile>()
        for (file in foundEpisodes) {
            extractEpisodeFormat(file.name)?.let {
                episodeMap[it] = file
            }
        }
        return episodeMap
    }

    private fun findMatchingEpisode(
        episode: Episode,
        episodeMap: Map<String, DriveFile>
    ): DriveFile? {
        val episodePattern = getEpisodePattern(episode)
        return episodeMap[episodePattern]
    }

    private fun getEpisodePattern(episode: Episode): String {
        return "S%02dE%02d".format(episode.season_number, episode.episode_number)
    }

    private fun extractEpisodeFormat(fileName: String): String? {
        try {
            val regex = Regex("""S(\d{2})E(\d+)""", RegexOption.IGNORE_CASE)
            val matchResult = regex.find(fileName)
            return matchResult?.value
        } catch (e: IndexOutOfBoundsException) {
            Log.d(TAG, "No match found for $fileName")
            e.printStackTrace()
        }
        return null
    }

    private fun createEpisodeModel(
        episode: Episode,
        fileId: String?
    ): EpisodesDataModel.Episode {
        return EpisodesDataModel.Episode(
            id = episode.id,
            name = episode.name ?: "TBA",
            overview = episode.name,
            episode_number = episode.episode_number,
            season_number = episode.season_number,
            still_path = episode.still_path,
            fileId = fileId
        )
    }

    private fun handleDefaultMapping(
        episodes: List<Episode>,
        seasonDataModel: MutableList<EpisodesDataModel>
    ) {
        Log.d(TAG, "Mapping attempt failed, using default")
        episodes.forEach {
            seasonDataModel.add(createEpisodeModel(it, fileId = null))
        }
    }

    private val _token = MutableLiveData<Event<Resource<FileToken>>>()
    val mpvFile: LiveData<Event<Resource<FileToken>>>
        get() = _token

    data class FileToken(
        val fileId: String,
        val fileName: String,
        val accessToken: String,
        val seasonNumber: Int,
        val episodeNumber: Int,
        val isLastEpisode: Boolean
    )

    fun playEpisode(
        title: String,
        seasonNumber: Int,
        episodeNumber: Int,
        isLastEpisode: Boolean,
        fileId: String
    ) = viewModelScope.launch {
        _token.postValue(Event(Resource.Loading()))

        val client = sessionManager.fetchClient() ?: run {
            _token.postValue(Event(Resource.Error("Client not found")))
            return@launch
        }
        val tokenResponse = driveRepository.fetchAccessToken(client)

        when (tokenResponse) {
            is Resource.Success -> {
                val fileToken = FileToken(
                    fileId = fileId,
                    fileName = title,
                    accessToken = tokenResponse.data!!.accessToken,
                    seasonNumber = seasonNumber,
                    episodeNumber = episodeNumber,
                    isLastEpisode = isLastEpisode
                )
                _token.postValue(Event(Resource.Success(fileToken)))
            }

            is Resource.Error -> {
                _token.postValue(
                    Event(Resource.Error(tokenResponse.message!!))
                )
            }

            else -> {}
        }
    }
}