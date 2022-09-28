package zechs.zplex.ui.cast

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.data.model.tmdb.person.PersonResponse
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.ui.cast.adapter.CastDataModel
import zechs.zplex.utils.state.Event
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.state.ResourceExt.Companion.postError
import javax.inject.Inject

@HiltViewModel
class CastViewModel @Inject constructor(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : BaseAndroidViewModel(app) {

    private val _personResponse = MutableLiveData<Event<Resource<List<CastDataModel>>>>()
    val personResponse: LiveData<Event<Resource<List<CastDataModel>>>>
        get() = _personResponse

    fun getPerson(personId: Int) = viewModelScope.launch {
        _personResponse.postValue(Event(Resource.Loading()))
        try {
            if (hasInternetConnection()) {
                val person = tmdbRepository.getPerson(personId)
                _personResponse.postValue(Event(handlePersonResponse(person)))
            } else {
                _personResponse.postValue(Event(Resource.Error("No internet connection")))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _personResponse.postValue(Event(postError(e)))
        }
    }


    private fun handlePersonResponse(
        response: Response<PersonResponse>
    ): Resource<List<CastDataModel>> {

        if (response.isSuccessful && response.body() != null) {
            val result = response.body()!!
            val castDataModel = mutableListOf<CastDataModel>()

            castDataModel.add(
                CastDataModel.Header(
                    name = result.name,
                    biography = result.biography,
                    profilePath = result.profile_path
                )
            )

            castDataModel.add(
                CastDataModel.Meta(
                    id = result.id,
                    age = result.age(),
                    birthday = result.birthday,
                    death = result.deathday,
                    gender = result.gender,
                    genderName = result.genderName,
                    place_of_birth = result.place_of_birth
                )
            )

            result.combined_credits.cast?.let { mediaList ->
                if (mediaList.isNotEmpty()) {
                    castDataModel.add(
                        CastDataModel.Heading(
                            heading = "Appears In"
                        )
                    )

                    castDataModel.add(
                        CastDataModel.AppearsIn(
                            appearsIn = mediaList.filter { m ->
                                m.releasedDate() != null && m.poster_path != null
                            }.sortedByDescending { m ->
                                m.releasedDate()!!.takeLast(4)
                            }.toList()
                        )
                    )

                }
            }

            return Resource.Success(castDataModel.toList())
        }
        return Resource.Error(response.message())
    }

}