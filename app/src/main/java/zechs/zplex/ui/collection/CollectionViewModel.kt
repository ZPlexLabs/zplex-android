package zechs.zplex.ui.collection

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.data.model.tmdb.collection.CollectionsResponse
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.ui.collection.adapter.CollectionDataModel
import zechs.zplex.utils.state.Event
import zechs.zplex.utils.state.Resource
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : BaseAndroidViewModel(app) {

    private val _collectionResponse = MutableLiveData<Event<Resource<List<CollectionDataModel>>>>()
    val collectionResponse: LiveData<Event<Resource<List<CollectionDataModel>>>>
        get() = _collectionResponse

    fun getCollection(collectionId: Int) = viewModelScope.launch {
        _collectionResponse.postValue(Event(Resource.Loading()))
        try {
            if (hasInternetConnection()) {
                val person = tmdbRepository.getCollection(collectionId)
                _collectionResponse.postValue(Event(handlePersonResponse(person)))
            } else {
                _collectionResponse.postValue(Event(Resource.Error("No internet connection")))
            }
        } catch (t: Throwable) {
            t.printStackTrace()

            val errorMsg = if (t is IOException) {
                "Network Failure"
            } else t.message ?: "Something went wrong"

            _collectionResponse.postValue(Event(Resource.Error(errorMsg)))
        }
    }


    private fun handlePersonResponse(
        response: Response<CollectionsResponse>
    ): Resource<List<CollectionDataModel>> {

        if (response.isSuccessful && response.body() != null) {
            val result = response.body()!!
            val collectionDataModel = mutableListOf<CollectionDataModel>()

            collectionDataModel.add(
                CollectionDataModel.Header(
                    title = result.name,
                    posterPath = result.poster_path,
                    backdropPath = result.backdrop_path
                )
            )

            val mediaList = result.parts
            if (mediaList.isNotEmpty()) {
                collectionDataModel.add(
                    CollectionDataModel.Parts(
                        parts = mediaList.sortedBy { m ->
                            m.releasedDate()?.takeLast(4)
                        }.toList()
                    )
                )
            }

            return Resource.Success(collectionDataModel.toList())
        }
        return Resource.Error(response.message())
    }
}