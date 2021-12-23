package zechs.zplex.ui.fragment.cast

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
import zechs.zplex.models.tmdb.person.CastResponse
import zechs.zplex.models.tmdb.person.CreditResponse
import zechs.zplex.models.tmdb.person.PersonResponse
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.utils.Resource
import java.io.IOException

class CastViewModel(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : AndroidViewModel(app) {

    val cast: MutableLiveData<Resource<CastResponse>> = MutableLiveData()

    fun getCredit(personId: Int, creditId: String) = viewModelScope.launch {
        cast.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val credit = tmdbRepository.getCredit(creditId)
                val people = tmdbRepository.getPeople(personId)
                cast.postValue(handleCastResponse(credit, people))
            } else {
                cast.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            println(t.stackTrace)
            println(t.message)
            cast.postValue(
                Resource.Error(
                    if (t is IOException) "Network Failure" else t.message ?: "Something went wrong"
                )
            )
        }
    }

    private fun handleCastResponse(
        credit: Response<CreditResponse>,
        people: Response<PersonResponse>
    ): Resource<CastResponse> {
        if (credit.isSuccessful && people.isSuccessful) {
            if (credit.body() != null && people.body() != null) {
                val creditResponse = credit.body()!!
                val peopleResponse = people.body()!!

                val castResponse = CastResponse(
                    gender = peopleResponse.gender,
                    known_for = creditResponse.person?.known_for ?: listOf(),
                    name = peopleResponse.name,
                    profile_path = peopleResponse.profile_path,
                    biography = peopleResponse.biography,
                    birthday = peopleResponse.birthday,
                    deathday = peopleResponse.deathday,
                    place_of_birth = peopleResponse.place_of_birth,
                    job = creditResponse.job
                )
                return Resource.Success(castResponse)
            }
        }
        return Resource.Error(people.message())
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