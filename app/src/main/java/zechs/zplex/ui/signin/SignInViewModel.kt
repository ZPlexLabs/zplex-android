package zechs.zplex.ui.signin

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zechs.zplex.common.utils.Resource
import zechs.zplex.googledrive.data.local.DriveClientStore
import zechs.zplex.googledrive.data.model.DriveClient
import zechs.zplex.googledrive.data.remote.api.token.model.AuthorizationResponse
import zechs.zplex.googledrive.data.repository.DriveRepository
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val driveRepository: Lazy<DriveRepository>,
    driveClientStore: DriveClientStore
) : ViewModel() {

    private val _loginStatus = MutableLiveData<Resource<AuthorizationResponse>>()
    val loginStatus: LiveData<Resource<AuthorizationResponse>>
        get() = _loginStatus

    val client = driveClientStore.flow()

    private var driveClient: DriveClient? = null

    fun getDriveClient() = driveClient

    fun setClient(
        clientId: String,
        clientSecret: String,
        redirectUri: String,
        scopes: List<String>
    ) {
        driveClient = DriveClient(
            clientId = clientId,
            clientSecret = clientSecret,
            redirectUri = redirectUri,
            scopes = scopes
        )
    }

    fun requestRefreshToken(
        authCodeUri: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        _loginStatus.postValue(Resource.Loading())
        val authCode = Uri.parse(authCodeUri).getQueryParameter("code")
        if (authCode == null) {
            _loginStatus.postValue(Resource.Error("Authorization code not found, please check url"))
        } else {
            if (driveClient == null) {
                _loginStatus.postValue(Resource.Error("Client not found, Make sure you have filled all the fields"))
                return@launch
            }
            val response = driveRepository.get().fetchRefreshToken(driveClient!!, authCode)
            _loginStatus.postValue(response)
        }
    }

}
