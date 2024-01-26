package zechs.zplex.ui.media

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.entities.Show
import zechs.zplex.data.model.tmdb.media.MovieResponse
import zechs.zplex.data.model.tmdb.media.TvResponse
import zechs.zplex.data.model.tmdb.search.SearchResponse
import zechs.zplex.data.repository.DriveRepository
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.ui.media.adapter.MediaDataModel
import zechs.zplex.utils.SessionManager
import zechs.zplex.utils.state.Event
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.state.ResourceExt.Companion.postError
import zechs.zplex.utils.util.Converter
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    app: Application,
    private val tmdbRepository: TmdbRepository,
    private val sessionManager: SessionManager,
    private val driveRepository: DriveRepository
) : BaseAndroidViewModel(app) {

    private val _dominantColor = MutableLiveData<Int>()
    val dominantColor: LiveData<Int>
        get() = _dominantColor

    fun saveShow(show: Show) = viewModelScope.launch {
        tmdbRepository.upsertShow(show)
    }

    fun deleteShow(tmdbId: Int) = viewModelScope.launch {
        tmdbRepository.deleteShow(tmdbId)
    }

    fun getShow(id: Int) = tmdbRepository.fetchShow(id)

    fun saveMovie(movie: Movie) = viewModelScope.launch {
        tmdbRepository.upsertMovie(movie)
    }

    fun deleteMovie(tmdbId: Int) = viewModelScope.launch {
        tmdbRepository.deleteMovie(tmdbId)
    }

    fun getMovie(id: Int) = tmdbRepository.fetchMovie(id)

    fun setDominantColor(color: Int) {
        if (_dominantColor.value == color) return
        _dominantColor.value = color
    }

    private val _mediaResponse = MutableLiveData<Resource<List<MediaDataModel>>>()
    val mediaResponse: LiveData<Resource<List<MediaDataModel>>>
        get() = _mediaResponse

    // Handling events or saving data
    var hasLoaded = false

    var tmdbId = 0
        private set

    var showName: String? = null
        private set

    var showPoster: String? = null
        private set

    var mediaType: MediaType = MediaType.tv

    fun getMedia(
        tmdbId: Int,
        mediaType: MediaType
    ) = viewModelScope.launch {
        _mediaResponse.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                when (mediaType) {
                    MediaType.tv -> fetchShow(tmdbId)
                    MediaType.movie -> fetchMovie(tmdbId)
                    else -> {}
                }
            } else {
                _mediaResponse.postValue(Resource.Error("No internet connection"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _mediaResponse.postValue(postError(e))
        }
    }

    private suspend fun fetchMovie(
        tmdbId: Int
    ) = withContext(Dispatchers.IO) {
        val page = 1
        val movie = tmdbRepository.getMovie(tmdbId)

        if (movie.isSuccessful && movie.body() != null) {
            movie.body()!!.production_companies?.let {
                if (it.isNotEmpty()) {
                    val moreFromCompany = tmdbRepository.getMoviesFromCompany(
                        it[0].id, page
                    )
                    val handleMovieResponse = handleMovieResponse(
                        movie, moreFromCompany
                    )
                    _mediaResponse.postValue(handleMovieResponse)
                    return@withContext
                }
            }
        }

        _mediaResponse.postValue(handleMovieResponse(movie, company = null))
    }

    private suspend fun fetchShow(
        tmdbId: Int
    ) = withContext(Dispatchers.IO) {
        val page = 1
        val tv = tmdbRepository.getShow(tmdbId)

        if (tv.isSuccessful && tv.body() != null) {
            tv.body()!!.production_companies?.let { companies ->
                if (companies.isNotEmpty()) {
                    val moreFromCompany = async {
                        tmdbRepository.getShowsFromCompany(
                            companyId = companies[0].id,
                            page = page
                        )
                    }
                    val handleTvResponse = handleTvResponse(
                        tv, moreFromCompany.await()
                    )
                    _mediaResponse.postValue(handleTvResponse)
                    return@withContext
                }
            }
        }

        _mediaResponse.postValue(handleTvResponse(tv, company = null))
    }


    private suspend fun handleTvResponse(
        response: Response<TvResponse>,
        company: Response<SearchResponse>?
    ): Resource<List<MediaDataModel>> {

        if (response.isSuccessful && response.body() != null) {
            val result = response.body()!!
            val mediaDataModel = mutableListOf<MediaDataModel>()

            this@MediaViewModel.showPoster = result.poster_path
            this@MediaViewModel.tmdbId = result.id
            this@MediaViewModel.mediaType = MediaType.tv
            this@MediaViewModel.showName = result.name

            val year: Int? = result.first_air_date?.let { firstAired ->
                if (firstAired.isNotBlank()) {
                    firstAired.take(4).toInt()
                } else null
            }

            var runtime = "${
                if (result.episode_run_time?.isNotEmpty() == true) {
                    result.episode_run_time[0]
                } else 0
            } min"

            runtime += if (year != null) {
                "  \u2022  $year"
            } else ""

            mediaDataModel.add(
                MediaDataModel.Header(
                    backdropPath = result.backdrop_path,
                    posterPath = result.poster_path,
                    rating = result.vote_average?.div(2) ?: 0.toDouble(),
                    genre = result.genres
                        ?.take(3)
                        ?.joinToString(
                            truncated = "",
                            separator = ", ",
                        ) { it.name }!!,
                    runtime = runtime
                )
            )

            val plot = if (result.overview == null || result.overview == "") {
                "No description"
            } else result.overview

            mediaDataModel.add(
                MediaDataModel.Title(
                    title = result.name!!,
                    plot = plot
                )
            )

            val seasonList = result.seasons?.toList() ?: listOf()
            result.last_episode_to_air?.let { ep ->
                val season = seasonList.firstOrNull {
                    it.season_number == ep.season_number
                }

                season?.let {
                    val seasonName = "Season ${it.season_number}"

                    var premiered = "$seasonName of ${result.name}"
                    var yearSeason = ""

                    val formattedDate = it.air_date?.let { date ->
                        yearSeason += "${date.take(4)} | "
                        Converter.parseDate(date)
                    }

                    yearSeason += "${it.episode_count} episodes"

                    formattedDate?.let {
                        premiered += " premiered on $formattedDate."
                    }
                    val seasonPlot = if (it.overview.isNullOrEmpty()) {
                        premiered
                    } else it.overview

                    mediaDataModel.add(
                        MediaDataModel.LatestSeason(
                            showTmdbId = result.id,
                            showName = result.name,
                            showPoster = result.poster_path,
                            seasonName = seasonName,
                            seasonPosterPath = season.poster_path,
                            seasonNumber = season.season_number,
                            seasonPlot = seasonPlot,
                            seasonYearAndEpisodeCount = yearSeason,
                        )
                    )
                }
            }

            val saved = tmdbRepository.fetchShowById(result.id)

            mediaDataModel.add(
                MediaDataModel.ShowButton(
                    show = Show(
                        id = result.id,
                        media_type = "tv",
                        name = result.name,
                        poster_path = result.poster_path,
                        vote_average = result.vote_average,
                        fileId = saved?.fileId
                    ),
                    seasons = seasonList
                )
            )

            result.credits?.cast?.let {
                if (it.isNotEmpty()) {
                    mediaDataModel.add(
                        MediaDataModel.Casts(
                            heading = "Casts",
                            casts = it
                        )
                    )
                }
            }

            result.recommendations?.results?.let {
                if (it.isNotEmpty() && it.size >= 3) {
                    mediaDataModel.add(
                        MediaDataModel.Recommendations(
                            heading = "Recommendations",
                            recommendations = it
                        )
                    )
                }
            }

            if (company != null && company.isSuccessful && company.body() != null) {
                val mediaList = company.body()!!.results
                if (mediaList.isNotEmpty() && mediaList.size >= 3) {
                    val studio = result.production_companies!![0].name!!

                    mediaDataModel.add(
                        MediaDataModel.MoreFromCompany(
                            heading = "More from $studio",
                            more = mediaList
                        )
                    )

                }
            }

            result.videos?.results?.let {
                if (it.isNotEmpty()) {
                    mediaDataModel.add(
                        MediaDataModel.Videos(
                            heading = "Related videos",
                            videos = it
                        )
                    )
                }
            }

            return Resource.Success(mediaDataModel.toList())
        }
        return Resource.Error(response.message())
    }

    private suspend fun handleMovieResponse(
        response: Response<MovieResponse>,
        company: Response<SearchResponse>?
    ): Resource<List<MediaDataModel>> {
        if (response.isSuccessful && response.body() != null) {
            val result = response.body()!!
            val mediaDataModel = mutableListOf<MediaDataModel>()

            this@MediaViewModel.showPoster = result.poster_path
            this@MediaViewModel.tmdbId = result.id
            this@MediaViewModel.mediaType = MediaType.movie
            this@MediaViewModel.showName = result.title

            val year: Int? = result.release_date?.let { releaseDate ->
                if (releaseDate.isNotBlank()) {
                    releaseDate.take(4).toInt()
                } else null
            }

            var runtime = ""
            result.runtime?.let {
                runtime += "$it min"
            }
            runtime += if (year != null) {
                "  \u2022  $year"
            } else ""

            mediaDataModel.add(
                MediaDataModel.Header(
                    backdropPath = result.backdrop_path,
                    posterPath = result.poster_path,
                    rating = result.vote_average?.div(2) ?: 0.toDouble(),
                    genre = result.genres?.take(3)?.joinToString(
                        truncated = "",
                        separator = ", ",
                    ) { it.name }!!,
                    runtime = runtime
                )
            )

            val plot = if (result.overview == null || result.overview == "") {
                "No description"
            } else result.overview

            mediaDataModel.add(
                MediaDataModel.Title(
                    title = result.title!!,
                    plot = plot
                )
            )

            result.belongs_to_collection?.let {
                mediaDataModel.add(
                    MediaDataModel.PartOfCollection(
                        bannerPoster = it.backdrop_path,
                        collectionName = it.name!!,
                        collectionId = it.id
                    )
                )
            }

            val saved = tmdbRepository.fetchMovieById(result.id)

            mediaDataModel.add(
                MediaDataModel.MovieButton(
                    movie = Movie(
                        id = result.id,
                        media_type = "movie",
                        title = result.title,
                        poster_path = result.poster_path,
                        vote_average = result.vote_average,
                        fileId = saved?.fileId
                    ),
                    year = year
                )
            )

            result.credits?.cast?.let {
                if (it.isNotEmpty()) {
                    mediaDataModel.add(
                        MediaDataModel.Casts(
                            heading = "Casts",
                            casts = it
                        )
                    )
                }
            }

            result.recommendations?.results?.let {
                if (it.isNotEmpty() && it.size >= 3) {
                    mediaDataModel.add(
                        MediaDataModel.Recommendations(
                            heading = "Recommendations",
                            recommendations = it
                        )
                    )
                }
            }

            if (company != null && company.isSuccessful && company.body() != null) {
                val mediaList = company.body()!!.results
                if (mediaList.isNotEmpty() && mediaList.size >= 3) {
                    val studio = result.production_companies!![0].name!!

                    mediaDataModel.add(
                        MediaDataModel.MoreFromCompany(
                            heading = "More from $studio",
                            more = mediaList
                        )
                    )
                }
            }

            result.videos?.results?.let {
                if (it.isNotEmpty()) {
                    mediaDataModel.add(
                        MediaDataModel.Videos(
                            heading = "Related videos",
                            videos = it
                        )
                    )
                }
            }

            hasLoaded = true
            return Resource.Success(mediaDataModel.toList())
        }
        return Resource.Error(response.message())
    }


    fun calcDominantColor(drawable: Drawable, onFinish: (Int) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(
            Bitmap.Config.ARGB_8888, true
        )
        Palette.from(bmp).generate { p ->
            try {
                onFinish(p!!.vibrantSwatch?.rgb ?: p.dominantSwatch?.rgb ?: 6770852)
            } catch (npe: NullPointerException) {
                onFinish(6770852)
            }
        }
    }

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

    private val _movieFile = MutableLiveData<Event<Resource<zechs.zplex.ui.player.Movie>>>()
    val movieFile: LiveData<Event<Resource<zechs.zplex.ui.player.Movie>>>
        get() = _movieFile

    fun playMovie(tmdbId: Int, year: Int?) = viewModelScope.launch {
        val saved = tmdbRepository.fetchMovieById(tmdbId)
        if (saved?.fileId == null) {
            _movieFile.postValue(Event(Resource.Error("Movie not found")))
            return@launch
        } else {
            val title = "${saved.title}${if (year != null) " (${year})" else ""}"
            val movie = zechs.zplex.ui.player.Movie(tmdbId, title, showPoster, saved.fileId)
            _movieFile.postValue(Event(Resource.Success(movie)))
        }
    }
}