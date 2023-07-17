package com.christidischristos.passkeys.screen.passkey

import android.app.Activity.RESULT_OK
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christidischristos.passkeys.repository.ApiResult
import com.christidischristos.passkeys.repository.webauthn.WebauthnRepository
import com.google.android.gms.fido.Fido
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
            is UserInteraction.OnActivityResultReceived -> onActivityResultReceived(userInteraction.activityResult)
        }
    }

    private fun initCreatePasskey() {
        viewModelScope.launch {
            when (val apiResult = webauthnRepository.initWebauthnRegistration()) {
                is ApiResult.HttpError -> showError("initWebauthnRegistration: ${apiResult.msg}")
                ApiResult.NetworkError -> showError("Network error!")
                is ApiResult.Success -> {
                    updateUiState { it.copy(optionsForIntent = apiResult.data) }
                }

                is ApiResult.GeneralError -> showError("initWebauthnRegistration: ${apiResult.msg}")
            }
        }
    }

    private fun onActivityResultReceived(activityResult: ActivityResult) {
        val dataBytes = activityResult.data?.getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA)
        when {
            activityResult.resultCode != RESULT_OK -> {
                showError("onActivityResultReceived: FIDO2 registration was cancelled")
            }

            dataBytes == null -> {
                showError("onActivityResultReceived: Error occurred on credential registration")
            }

            else -> {
                val credential = PublicKeyCredential.deserializeFromBytes(dataBytes)
                val response = credential.response
                if (response is AuthenticatorErrorResponse) {
                    showError("CredentialCreation failed: ${response.errorCode} - ${response.errorMessage}")
                } else {
                    viewModelScope.launch {
                        when (
                            val apiResult =
                                webauthnRepository.finalizeWebauthnRegistration(credential)
                        ) {
                            is ApiResult.HttpError -> showError("finalizeWebauthnRegistration: ${apiResult.msg}")
                            ApiResult.NetworkError -> showError("Network error!")
                            is ApiResult.Success -> updateUiState { it.copy(goToHomeScreen = true) }
                            is ApiResult.GeneralError -> showError("finalizeWebauthnRegistration: ${apiResult.msg}")
                        }
                    }
                }
            }

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
        val optionsForIntent: PublicKeyCredentialCreationOptions? = null,
        val goToHomeScreen: Boolean = false
    )

    sealed interface UserInteraction {
        object OnCreatePasskeyClicked : UserInteraction
        object OnSkipClicked : UserInteraction
        data class OnActivityResultReceived(val activityResult: ActivityResult) : UserInteraction
    }
}
