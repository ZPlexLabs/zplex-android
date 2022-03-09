package zechs.zplex.ui.fragment.music

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import zechs.zplex.models.dataclass.ConstantsResult
import zechs.zplex.models.drive.DriveResponse
import zechs.zplex.repository.FilesRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.utils.Constants.CLIENT_ID
import zechs.zplex.utils.Constants.CLIENT_SECRET
import zechs.zplex.utils.Constants.DOCUMENT_PATH
import zechs.zplex.utils.Constants.REFRESH_TOKEN
import zechs.zplex.utils.Constants.TEMP_TOKEN
import zechs.zplex.utils.Constants.ZPLEX
import zechs.zplex.utils.Constants.ZPLEX_DRIVE_ID
import zechs.zplex.utils.Constants.ZPLEX_MUSIC_ID
import zechs.zplex.utils.Constants.ZPLEX_SHOWS_ID
import zechs.zplex.utils.Event
import zechs.zplex.utils.Resource
import zechs.zplex.utils.SessionManager
import java.io.IOException

class MusicViewModel(
    app: Application,
    private val filesRepository: FilesRepository
) : BaseAndroidViewModel(app) {

    private val pageSize = 1000
    private val orderBy = "name"

    private val accessToken = SessionManager(
        getApplication<Application>().applicationContext
    ).fetchAuthToken()

    private val database = Firebase.firestore

    val driveList: MutableLiveData<Event<Resource<DriveResponse>>> = MutableLiveData()

    init {
        openDriveFolder(ZPLEX_MUSIC_ID)
    }

    fun openDriveFolder(folderId: String) = viewModelScope.launch {
        driveList.postValue(Event(Resource.Loading()))
        try {
            if (hasInternetConnection()) {
                getCredentials(folderId)
            } else {
                driveList.postValue(Event(Resource.Error("No internet connection")))
            }
        } catch (t: Throwable) {
            println(t.stackTrace)
            println(t.message)
            driveList.postValue(
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

    private fun getCredentials(folderId: String) {
        database.collection("constants")
            .document(DOCUMENT_PATH)
            .get()
            .addOnSuccessListener {
                cloudStoreSuccess(it, folderId)
            }
    }

    private fun cloudStoreSuccess(
        documentSnapshot: DocumentSnapshot?, folderId: String
    ) {
        val constantsResult = documentSnapshot?.toObject<ConstantsResult>()
        constantsResult?.let {
            ZPLEX = it.zplex
            ZPLEX_DRIVE_ID = it.zplex_drive_id
            ZPLEX_SHOWS_ID = it.zplex_shows_id
            CLIENT_ID = it.client_id
            CLIENT_SECRET = it.client_secret
            REFRESH_TOKEN = it.refresh_token
            TEMP_TOKEN = it.temp_token
            driveCall(it, folderId)
        }
    }

    private fun driveCall(
        it: ConstantsResult,
        folderId: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        val folder = filesRepository.getDriveFiles(
            pageSize = pageSize,
            if (accessToken == "") it.temp_token else accessToken,
            pageToken = null,
            folderQuery(folderId = folderId),
            orderBy = orderBy
        )
        driveList.postValue(Event(handleDriveResponse(folder)))
    }

    private fun handleDriveResponse(
        response: Response<DriveResponse>
    ): Resource<DriveResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun folderQuery(folderId: String) = "'${folderId}' in parents and trashed = false"
}