package zechs.zplex.ui.fragment.media

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.adapter.media.MediaDataModel
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.models.enum.MediaType
import zechs.zplex.models.tmdb.media.TvResponse
import zechs.zplex.models.tmdb.search.SearchResponse
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.repository.ZPlexRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.ui.movieResponseTmdb
import zechs.zplex.ui.movieResponseZplex
import zechs.zplex.utils.ConverterUtils
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

    private val _dominantColor = MutableLiveData<Int>()
    val dominantColor: LiveData<Int>
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
        if (_dominantColor.value == color) return
        _dominantColor.value = color
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

    private val _mediaResponse = MutableLiveData<Event<Resource<List<MediaDataModel>>>>()
    val mediaResponse: LiveData<Event<Resource<List<MediaDataModel>>>>
        get() = _mediaResponse

    fun getMedia(tmdbId: Int, mediaType: MediaType) = viewModelScope.launch {
        _mediaResponse.postValue(Event(Resource.Loading()))
        try {
            if (hasInternetConnection()) {
                when (mediaType) {
                    MediaType.tv -> fetchShow(this, tmdbId)
                    MediaType.movie -> fetchMovie(this, tmdbId)
                }
            } else {
                _mediaResponse.postValue(Event(Resource.Error("No internet connection")))
            }
        } catch (t: Throwable) {
            t.printStackTrace()

            val errorMsg = if (t is IOException) {
                "Network Failure"
            } else t.message ?: "Something went wrong"

            _mediaResponse.postValue(Event(Resource.Error(errorMsg)))
        }
    }

    private fun fetchMovie(
        scope: CoroutineScope, tmdbId: Int
    ) = scope.launch {
        val page = 1
        val movieResponse = async { tmdbRepository.getMovie(tmdbId) }
        val movie = movieResponse.await()

        if (movie.isSuccessful && movie.body() != null) {
            movie.body()!!.production_companies?.let {
                if (it.isNotEmpty()) {
                    val moreFromCompany = async {
                        tmdbRepository.getMoviesFromCompany(
                            it[0].id, page
                        )
                    }
                    val handleTvResponse = handleMovieResponse(
                        movie, moreFromCompany.await()
                    )
                    _mediaResponse.postValue(Event(handleTvResponse))
                }
            }
        } else {
            _mediaResponse.postValue(Event(handleMovieResponse(movie, null)))
        }
    }

    private fun fetchShow(
        scope: CoroutineScope, tmdbId: Int
    ) = scope.launch {
        val page = 1
        val tvResponse = async { tmdbRepository.getShow(tmdbId) }
        val tv = tvResponse.await()

        if (tv.isSuccessful && tv.body() != null) {
            tv.body()!!.production_companies?.let {
                if (it.isNotEmpty()) {
                    val moreFromCompany = async {
                        tmdbRepository.getShowsFromCompany(
                            it[0].id, page
                        )
                    }
                    val handleTvResponse = handleTvResponse(
                        tv, moreFromCompany.await()
                    )
                    _mediaResponse.postValue(Event(handleTvResponse))
                }
            }
        } else {
            _mediaResponse.postValue(Event(handleTvResponse(tv, null)))
        }
    }


    private fun handleTvResponse(
        response: Response<TvResponse>,
        company: Response<SearchResponse>?
    ): Resource<List<MediaDataModel>> {

        if (response.isSuccessful && response.body() != null) {
            val result = response.body()!!
            val mediaDataModel = mutableListOf<MediaDataModel>()

            val year = result.first_air_date?.take(4)?.toInt()

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
                        ConverterUtils.parseDate(date)
                    }

                    yearSeason += "${it.episode_count} episodes"

                    formattedDate?.let {
                        premiered += " premiered on $formattedDate."
                    }
                    val seasonPlot = if (it.overview == null || it.overview.toString() == "") {
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

            mediaDataModel.add(
                MediaDataModel.ShowButton(
                    show = Show(
                        id = result.id,
                        media_type = "tv",
                        name = result.name,
                        poster_path = result.poster_path,
                        vote_average = result.vote_average,
                    ),
                    seasons = seasonList
                )
            )

            result.credits.cast?.let {
                if (it.isNotEmpty()) {
                    mediaDataModel.add(
                        MediaDataModel.Heading(heading = "Casts")
                    )
                    mediaDataModel.add(
                        MediaDataModel.Casts(casts = it)
                    )
                }
            }

            result.recommendations?.results?.let {
                if (it.isNotEmpty() && it.size >= 3) {
                    mediaDataModel.add(
                        MediaDataModel.Heading(heading = "Recommendations")
                    )
                    mediaDataModel.add(
                        MediaDataModel.Recommendations(recommendations = it)
                    )
                }
            }

            if (company != null && company.isSuccessful && company.body() != null) {
                val mediaList = company.body()!!.results
                if (mediaList.isNotEmpty() && mediaList.size >= 3) {
                    val studio = result.production_companies!![0].name!!

                    mediaDataModel.add(
                        MediaDataModel.Heading(heading = "More from $studio")
                    )

                    mediaDataModel.add(
                        MediaDataModel.MoreFromCompany(more = mediaList)
                    )

                }
            }

            result.videos?.results?.let {
                if (it.isNotEmpty()) {
                    mediaDataModel.add(
                        MediaDataModel.Heading(heading = "Related videos")
                    )
                    mediaDataModel.add(
                        MediaDataModel.Videos(videos = it)
                    )
                }
            }

            return Resource.Success(mediaDataModel.toList())
        }
        return Resource.Error(response.message())
    }

    private fun handleMovieResponse(
        response: Response<movieResponseTmdb>,
        company: Response<SearchResponse>?
    ): Resource<List<MediaDataModel>> {
        if (response.isSuccessful && response.body() != null) {
            val result = response.body()!!
            val mediaDataModel = mutableListOf<MediaDataModel>()

            val year: Int? = result.release_date?.let { firstAired ->
                if (firstAired.isNotBlank()) {
                    firstAired.take(4).toInt()
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

            mediaDataModel.add(
                MediaDataModel.MovieButton(
                    movie = Movie(
                        id = result.id,
                        media_type = "movie",
                        title = result.title,
                        poster_path = result.poster_path,
                        vote_average = result.vote_average
                    )
                )
            )

            result.credits.cast?.let {
                if (it.isNotEmpty()) {
                    mediaDataModel.add(
                        MediaDataModel.Heading(heading = "Casts")
                    )
                    mediaDataModel.add(
                        MediaDataModel.Casts(casts = it)
                    )
                }
            }

            result.recommendations?.results?.let {
                if (it.isNotEmpty() && it.size >= 3) {
                    mediaDataModel.add(
                        MediaDataModel.Heading(heading = "Recommendations")
                    )
                    mediaDataModel.add(
                        MediaDataModel.Recommendations(recommendations = it)
                    )
                }
            }

            if (company != null && company.isSuccessful && company.body() != null) {
                val mediaList = company.body()!!.results
                if (mediaList.isNotEmpty() && mediaList.size >= 3) {
                    val studio = result.production_companies!![0].name!!
                    mediaDataModel.add(
                        MediaDataModel.Heading(heading = "More from $studio")
                    )
                    mediaDataModel.add(
                        MediaDataModel.MoreFromCompany(more = mediaList)
                    )
                }
            }

            result.videos?.results?.let {
                if (it.isNotEmpty()) {
                    mediaDataModel.add(
                        MediaDataModel.Heading(heading = "Related videos")
                    )
                    mediaDataModel.add(
                        MediaDataModel.Videos(videos = it)
                    )
                }
            }

            return Resource.Success(mediaDataModel.toList())
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