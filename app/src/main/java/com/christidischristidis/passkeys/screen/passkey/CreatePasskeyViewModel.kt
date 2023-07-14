package com.christidischristidis.passkeys.screen.passkey

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christidischristidis.passkeys.network.model.FinalizeWebauthnRegistrationRequest
import com.christidischristidis.passkeys.network.model.InitWebauthnRegistrationRequest
import com.christidischristidis.passkeys.repository.ApiResult
import com.christidischristidis.passkeys.repository.webauthn.WebauthnRepository
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
            UserInteraction.OnCreatePasskeyClicked -> initCreatePasskey()
            UserInteraction.OnSkipClicked -> onSkipClicked()
        }
    }

    private fun initCreatePasskey() {
        viewModelScope.launch {
            when (val apiResult = webauthnRepository.initWebauthnRegistration(
                InitWebauthnRegistrationRequest("foo")
            )) {
                is ApiResult.HttpError -> showError("initWebauthnRegistration: ${apiResult.msg}")
                ApiResult.NetworkError -> showError("Network error!")
                is ApiResult.Success -> {
                    finalizeCreatePasskey()
                }

                is ApiResult.GeneralError -> showError("initWebauthnRegistration: ${apiResult.msg}")
            }
        }
    }

    private suspend fun finalizeCreatePasskey() {
        when (val apiResult = webauthnRepository.finalizeWebauthnRegistration(
            FinalizeWebauthnRegistrationRequest("foo")
        )) {
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
        object OnCreatePasskeyClicked : UserInteraction
        object OnSkipClicked : UserInteraction
    }
}
