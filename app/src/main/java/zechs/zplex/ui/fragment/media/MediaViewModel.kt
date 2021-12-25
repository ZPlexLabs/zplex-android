package zechs.zplex.ui.fragment.media

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
import zechs.zplex.models.tmdb.collection.CollectionsResponse
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.models.tmdb.entities.Pair
import zechs.zplex.models.tmdb.media.MediaResponse
import zechs.zplex.models.tmdb.media.MovieResponse
import zechs.zplex.models.tmdb.media.TvResponse
import zechs.zplex.repository.FilesRepository
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.utils.Constants
import zechs.zplex.utils.ConverterUtils
import zechs.zplex.utils.Resource
import zechs.zplex.utils.SessionManager
import java.io.IOException

class MediaViewModel(
    app: Application,
    private val filesRepository: FilesRepository,
    private val tmdbRepository: TmdbRepository
) : AndroidViewModel(app) {

    private val accessToken = SessionManager(
        getApplication<Application>().applicationContext
    ).fetchAuthToken()

    val searchList: MutableLiveData<Resource<DriveResponse>> = MutableLiveData()
    val media: MutableLiveData<Resource<MediaResponse>> = MutableLiveData()

    fun saveShow(media: Media) = viewModelScope.launch {
        tmdbRepository.upsertMedia(media)
    }

    fun deleteShow(media: Media) = viewModelScope.launch {
        tmdbRepository.deleteMedia(media)
    }

    fun getShow(id: Int) = tmdbRepository.getMedia(id)

    fun getMedia(tmdbId: Int, mediaType: String) = viewModelScope.launch {
        media.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                if (mediaType == "movie") {
                    val response = tmdbRepository.getMovie(tmdbId)
                    if (response.body()?.belongs_to_collection != null) {
                        val collectionsResponse = tmdbRepository.getCollection(
                            collectionId = response.body()?.belongs_to_collection!!.id
                        )
                        media.postValue(
                            handleMovieResponseWithCollection(
                                response,
                                collectionsResponse
                            )
                        )
                    } else {
                        media.postValue(handleMovieResponse(response))
                    }
                } else {
                    val response = tmdbRepository.getShow(tmdbId)
                    media.postValue(handleTvResponse(response))
                }
            } else {
                media.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            println(t.stackTrace)
            println(t.message)
            media.postValue(
                Resource.Error(
                    if (t is IOException) "Network Failure" else t.message ?: "Something went wrong"
                )
            )
        }
    }

    private fun handleMovieResponseWithCollection(
        response: Response<MovieResponse>,
        collectionsResponse: Response<CollectionsResponse>
    ): Resource<MediaResponse> {
        if (response.isSuccessful && collectionsResponse.isSuccessful) {
            if (response.body() != null && collectionsResponse.body() != null) {
                val resultResponse = response.body()!!
                val collectionsResult = collectionsResponse.body()!!

                val ratingText = "${resultResponse.vote_average}/10"
                val durationText = if (resultResponse.runtime == null) {
                    "Unknown"
                } else ConverterUtils.convertMinutes(resultResponse.runtime)

                val genreResponse = if (resultResponse.genres?.isEmpty() == true) {
                    "Unknown"
                } else resultResponse.genres?.get(0)?.name ?: "Unknown"

                val genreText = if (genreResponse == "Science Fiction") {
                    "Sci-Fi"
                } else genreResponse.substringBefore(" ")

                val pairsArray: MutableList<Pair> = mutableListOf()
                pairsArray.add(Pair("Genre", genreText))
                pairsArray.add(Pair("Rating", ratingText))
                pairsArray.add(Pair("Duration", durationText))

                resultResponse.production_companies?.let { company ->
                    if (company.isNotEmpty()) company[0].name?.let {
                        Pair("Studio", it)
                    }?.let { pairsArray.add(it) }
                }

                resultResponse.release_date?.let { firstAired ->
                    if (firstAired.isNotEmpty()) pairsArray.add(
                        Pair("Released", firstAired.take(4))
                    )
                }

                val videosList = resultResponse.videos?.results?.filter { v ->
                    v.site == "YouTube"
                } ?: listOf()


                val mediaResponse = MediaResponse(
                    id = resultResponse.id,
                    name = resultResponse.title,
                    overview = resultResponse.overview,
                    poster_path = resultResponse.poster_path,
                    related_media = collectionsResult.parts,
                    misc = pairsArray,
                    seasons = listOf(),
                    cast = resultResponse.credits.cast?.toList() ?: listOf(),
                    recommendations = resultResponse.recommendations?.results?.toList() ?: listOf(),
                    similar = resultResponse.similar?.results?.toList() ?: listOf(),
                    videos = videosList,
                    vote_average = "${resultResponse.vote_average}/10"
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
                val ratingText = "${resultResponse.vote_average}/10"
                val durationText = if (resultResponse.episode_run_time?.isEmpty() == true) {
                    "Unknown"
                } else ConverterUtils.convertMinutes(resultResponse.episode_run_time?.get(0) ?: 0)

                val genreResponse = if (resultResponse.genres?.isEmpty() == true) {
                    "Unknown"
                } else resultResponse.genres?.get(0)?.name ?: "Unknown"

                val genreText = if (genreResponse == "Science Fiction") {
                    "Sci-Fi"
                } else genreResponse.substringBefore(" ")

                val pairsArray: MutableList<Pair> = mutableListOf()
                pairsArray.add(Pair("Genre", genreText))
                pairsArray.add(Pair("Rating", ratingText))
                pairsArray.add(Pair("Duration", durationText))

                resultResponse.networks?.let { network ->
                    if (network.isNotEmpty())
                        network[0].name?.let { Pair("Network", it) }?.let { pairsArray.add(it) }
                }

                resultResponse.first_air_date?.let { firstAired ->
                    if (firstAired.isNotEmpty())
                        pairsArray.add(
                            Pair(
                                "Released",
                                firstAired.take(4)
                            )
                        )
                }

                val videosList = resultResponse.videos?.results?.filter { v ->
                    v.site == "YouTube"
                } ?: listOf()

                val mediaResponse = MediaResponse(
                    id = resultResponse.id,
                    name = resultResponse.name,
                    overview = resultResponse.overview,
                    poster_path = resultResponse.poster_path,
                    related_media = listOf(),
                    misc = pairsArray,
                    seasons = resultResponse.seasons ?: listOf(),
                    cast = resultResponse.credits.cast?.toList() ?: listOf(),
                    recommendations = resultResponse.recommendations?.results?.toList() ?: listOf(),
                    similar = resultResponse.similar?.results?.toList() ?: listOf(),
                    videos = videosList,
                    vote_average = "${resultResponse.vote_average}/10"
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

                val ratingText = "${resultResponse.vote_average}/10"
                val durationText = if (resultResponse.runtime == null) {
                    "Unknown"
                } else ConverterUtils.convertMinutes(resultResponse.runtime)

                val genreResponse = if (resultResponse.genres?.isEmpty() == true) {
                    "Unknown"
                } else resultResponse.genres?.get(0)?.name ?: "Unknown"

                val genreText = if (genreResponse == "Science Fiction") {
                    "Sci-Fi"
                } else genreResponse.substringBefore(" ")

                val pairsArray: MutableList<Pair> = mutableListOf()
                pairsArray.add(Pair("Genre", genreText))
                pairsArray.add(Pair("Rating", ratingText))
                pairsArray.add(Pair("Duration", durationText))

                resultResponse.production_companies?.let { company ->
                    if (company.isNotEmpty()) company[0].name?.let {
                        Pair("Studio", it)
                    }?.let { pairsArray.add(it) }
                }

                resultResponse.release_date?.let { firstAired ->
                    if (firstAired.isNotEmpty()) pairsArray.add(
                        Pair("Released", firstAired.take(4))
                    )
                }

                val videosList = resultResponse.videos?.results?.filter { v ->
                    v.site == "YouTube"
                } ?: listOf()

                val mediaResponse = MediaResponse(
                    id = resultResponse.id,
                    name = resultResponse.title,
                    overview = resultResponse.overview,
                    poster_path = resultResponse.poster_path,
                    related_media = listOf(),
                    misc = pairsArray,
                    seasons = listOf(),
                    cast = resultResponse.credits.cast?.toList() ?: listOf(),
                    recommendations = resultResponse.recommendations?.results?.toList() ?: listOf(),
                    similar = resultResponse.similar?.results?.toList() ?: listOf(),
                    videos = videosList,
                    vote_average = "${resultResponse.vote_average}/10"
                )
                return Resource.Success(mediaResponse)
            }
        }
        return Resource.Error(response.message())
    }


    fun doSearchFor(searchQuery: String) =
        viewModelScope.launch {
            searchList.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = filesRepository.getDriveFiles(
                        pageSize = 1,
                        if (accessToken == "") Constants.TEMP_TOKEN else accessToken,
                        pageToken = "", searchQuery, orderBy = "modifiedTime desc"
                    )
                    searchList.postValue(handleSearchListResponse(response))
                } else {
                    searchList.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)
                searchList.postValue(
                    Resource.Error(
                        if (t is IOException) {
                            "Network Failure"
                        } else t.message ?: "Something went wrong"
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
}