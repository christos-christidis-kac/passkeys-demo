package com.christidischristidis.passkeys.screen.passkey

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialInterruptedException
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.CreateCustomCredentialException
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christidischristidis.passkeys.repository.ApiResult
import com.christidischristidis.passkeys.repository.webauthn.WebauthnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class CreatePasskeyViewModel @Inject constructor(
    private val webauthnRepository: WebauthnRepository,
    private val application: Application
) : ViewModel() {

    private val credentialManager by lazy { CredentialManager.create(application) }

    private var _uiState: MutableState<UiState> = mutableStateOf(UiState())
    val uiState: State<UiState> = _uiState

    private fun updateUiState(updateFunc: (UiState) -> UiState) {
        _uiState.value = updateFunc(uiState.value)
    }

    fun onUserInteraction(userInteraction: UserInteraction) {
        when (userInteraction) {
            is UserInteraction.OnCreatePasskeyClicked -> initCreatePasskey(userInteraction.activity)
            UserInteraction.OnSkipClicked -> onSkipClicked()
        }
    }

    private fun initCreatePasskey(activity: Activity) {
        viewModelScope.launch {
            when (val apiResult = webauthnRepository.initWebauthnRegistration()) {
                is ApiResult.HttpError -> showError("initWebauthnRegistration: ${apiResult.msg}")
                ApiResult.NetworkError -> showError("Network error!")
                is ApiResult.Success -> {
                    val jsonStringForManager = Json.encodeToString(apiResult.data.publicKey)
                    createPasskey(activity, jsonStringForManager)?.let {
                        finalizePasskey(it)
                    }
                }

                is ApiResult.GeneralError -> showError("initWebauthnRegistration: ${apiResult.msg}")
            }
        }
    }

    private suspend fun createPasskey(
        activity: Activity,
        initRegistrationJson: String
    ): CreatePublicKeyCredentialResponse? {
        val request = CreatePublicKeyCredentialRequest(initRegistrationJson)
        var response: CreatePublicKeyCredentialResponse? = null
        try {
            response = credentialManager.createCredential(
                request, activity
            ) as CreatePublicKeyCredentialResponse
        } catch (e: CreateCredentialException) {
            handleCreatePasskeyError(e)
        }
        return response
    }

    private fun handleCreatePasskeyError(e: CreateCredentialException) {
        val msg = when (e) {
            is CreatePublicKeyCredentialDomException -> {
                // Handle the passkey DOM errors thrown according to the WebAuthn spec using e.domError
                "An error occurred while creating a passkey"
            }

            is CreateCredentialCancellationException -> {
                // The user intentionally canceled the operation
                "Credential registration cancelled by user!"
            }

            is CreateCredentialInterruptedException -> {
                // Retry-able error. Consider retrying the call.
                "The operation was interrupted, please retry the call"
            }

            is CreateCredentialProviderConfigurationException -> {
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
                // that SDK to match with e.type. Otherwise, drop or log the
                // exception.
                "An unknown error occurred from a 3rd party SDK"
            }

            else -> {
                Log.w("Auth", "Unexpected exception type ${e::class.java.name}")
                "An unknown error occurred."
            }
        }
        Log.e("Auth", "createPasskey failed with exception: " + e.message.toString())
        showError(msg)
    }

    private suspend fun finalizePasskey(credentialManagerResponse: CreatePublicKeyCredentialResponse) {
        when (
            val apiResult = webauthnRepository.finalizeWebauthnRegistration(
                credentialManagerResponse.registrationResponseJson
            )
        ) {
            is ApiResult.HttpError -> showError("finalizeWebauthnRegistration: ${apiResult.msg}")
            ApiResult.NetworkError -> showError("Network error!")
            is ApiResult.Success -> {
                updateUiState { it.copy(goToHomeScreen = true) }
            }

            is ApiResult.GeneralError -> showError("finalizeWebauthnRegistration: ${apiResult.msg}")
        }
    }

    private fun onSkipClicked() {
        updateUiState { it.copy(goToHomeScreen = true) }
    }

    private fun showError(text: String) {
        updateUiState { it.copy(error = text) }
    }

    data class UiState(
        val error: String? = null,
        val goToHomeScreen: Boolean = false
    )

    sealed interface UserInteraction {
        data class OnCreatePasskeyClicked(val activity: Activity) : UserInteraction
        object OnSkipClicked : UserInteraction
    }
}
