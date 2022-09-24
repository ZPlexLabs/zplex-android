package zechs.zplex.ui.fragment.watch

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.ThisApp
import zechs.zplex.models.tmdb.entities.Episode
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.utils.Resource
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class WatchViewModel @Inject constructor(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : AndroidViewModel(app) {

    val episode: MutableLiveData<Resource<Episode>> = MutableLiveData()

    fun getEpisode(tvId: Int, seasonNumber: Int, episodeNumber: Int) =
        viewModelScope.launch {
            episode.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = tmdbRepository.getEpisode(tvId, seasonNumber, episodeNumber)
                    episode.postValue(handleEpisodeResponse(response))
                } else {
                    episode.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                println(t.stackTrace)
                println(t.message)
                episode.postValue(
                    Resource.Error(
                        if (t is IOException) {
                            "Network Failure"
                        } else t.message ?: "Something went wrong"
                    )
                )
            }
        }

    private fun handleEpisodeResponse(
        response: Response<Episode>
    ): Resource<Episode> {
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