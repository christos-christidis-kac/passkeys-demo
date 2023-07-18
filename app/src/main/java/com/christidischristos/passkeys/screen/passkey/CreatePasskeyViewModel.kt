package com.christidischristos.passkeys.screen.passkey

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialInterruptedException
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.CreateCustomCredentialException
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christidischristos.passkeys.network.model.MyCreatePublicKeyResponse
import com.christidischristos.passkeys.repository.ApiResult
import com.christidischristos.passkeys.repository.webauthn.WebauthnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class CreatePasskeyViewModel @Inject constructor(
    private val webauthnRepository: WebauthnRepository
) : ViewModel() {

    private var _uiState: MutableState<UiState> = mutableStateOf(UiState())
    val uiState: State<UiState> = _uiState

    private fun updateUiState(updateFunc: (UiState) -> UiState) {
        _uiState.value = updateFunc(uiState.value)
    }

    fun onUserInteraction(userInteraction: UserInteraction) {
        when (userInteraction) {
            is UserInteraction.OnCreatePasskeyClicked -> initCreatePasskey()
            UserInteraction.OnSkipClicked -> onSkipClicked()
            is UserInteraction.OnCreatePasskeyException -> onCreatePasskeyException(userInteraction.e)
            is UserInteraction.OnCreatePasskeySuccess -> onCreatePasskeySuccess(userInteraction.response)
        }
    }

    private fun initCreatePasskey() {
        viewModelScope.launch {
            when (val apiResult = webauthnRepository.initWebauthnRegistration()) {
                is ApiResult.HttpError -> showError("initWebauthnRegistration: ${apiResult.msg}")
                ApiResult.NetworkError -> showError("Network error!")
                is ApiResult.Success -> {
                    updateUiState { it.copy(createKeyRequest = apiResult.data) }
                }

                is ApiResult.GeneralError -> showError("initWebauthnRegistration: ${apiResult.msg}")
            }
        }
    }

    private fun onSkipClicked() {
        updateUiState { it.copy(goToHomeScreen = true) }
    }

    private fun onCreatePasskeySuccess(response: CreatePublicKeyCredentialResponse) {
        val response = Json.decodeFromString<MyCreatePublicKeyResponse>(
            response.registrationResponseJson
        )
        viewModelScope.launch {
            when (val apiResult = webauthnRepository.finalizeWebauthnRegistration(response)) {
                is ApiResult.HttpError -> showError("finalizeWebauthnRegistration: ${apiResult.msg}")
                ApiResult.NetworkError -> showError("Network error!")
                is ApiResult.Success -> updateUiState { it.copy(goToHomeScreen = true) }
                is ApiResult.GeneralError -> showError("finalizeWebauthnRegistration: ${apiResult.msg}")
            }
        }
    }

    private fun onCreatePasskeyException(exception: CreateCredentialException) {
        val msg = when (exception) {
            is CreatePublicKeyCredentialDomException -> {
                // Handle the passkey DOM errors thrown according to Webauthn spec using e.domError
                "An error occurred while creating a passkey"
            }

            is CreateCredentialCancellationException -> {
                // The user intentionally canceled the operation to register the credential.
                "The user intentionally canceled the operation"
            }

            is CreateCredentialInterruptedException -> {
                // Retry-able error. Consider retrying the call.
                "Operation was interrupted, please retry"
            }

            is CreateCredentialProviderConfigurationException -> {
                // Your app is missing the provider configuration dependency.
                // Most likely, you're missing "credentials-play-services-auth".
                "Your app is missing the provider configuration dependency"
            }

            is CreateCredentialUnknownException -> {
                "An unknown error occurred while creating passkey"
            }

            is CreateCustomCredentialException -> {
                // You have encountered an error from a 3rd-party SDK. If you
                // make the API call with a request object that's a subclass of
                // CreateCustomCredentialRequest using a 3rd-party SDK, then you
                // should check for any custom exception type constants within
                // that SDK to match with e.type.
                "An unknown error occurred from a 3rd party SDK. Check logs for additional details"
            }

            else -> "An unknown error occurred"
        }
        showError(msg)
    }

    private fun showError(text: String) {
        updateUiState { it.copy(error = text) }
    }

    data class UiState(
        val error: String? = null,
        val createKeyRequest: CreatePublicKeyCredentialRequest? = null,
        val goToHomeScreen: Boolean = false
    )

    sealed interface UserInteraction {
        object OnCreatePasskeyClicked : UserInteraction
        object OnSkipClicked : UserInteraction
        data class OnCreatePasskeySuccess(val response: CreatePublicKeyCredentialResponse) :
            UserInteraction

        data class OnCreatePasskeyException(val e: CreateCredentialException) : UserInteraction
    }
}
