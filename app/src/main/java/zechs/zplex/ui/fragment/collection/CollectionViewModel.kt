package zechs.zplex.ui.fragment.collection

import android.app.Application
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.channelFlow
import retrofit2.Response
import zechs.zplex.models.tmdb.collection.CollectionsResponse
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.utils.Resource
import java.io.IOException

class CollectionViewModel(
    app: Application,
    private val tmdbRepository: TmdbRepository,
) : BaseAndroidViewModel(app) {

    fun getCollection(collectionId: Int) = channelFlow {
        send(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val collectionsResponse = tmdbRepository.getCollection(collectionId)
                send(handleCollectionResponse(collectionsResponse))
            } else {
                send(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            println(t.stackTrace)
            println(t.message)
            val post = if (t is IOException) {
                "Network Failure"
            } else t.message ?: "Something went wrong"
            send(Resource.Error(post))
        }
    }.asLiveData()


    private fun handleCollectionResponse(
        response: Response<CollectionsResponse>
    ): Resource<CollectionsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

}