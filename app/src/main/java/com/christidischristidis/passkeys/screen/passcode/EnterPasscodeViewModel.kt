package com.christidischristidis.passkeys.screen.passcode

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christidischristidis.passkeys.repository.ApiResult
import com.christidischristidis.passkeys.repository.passcode.PasscodeRepository
import com.christidischristidis.passkeys.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnterPasscodeViewModel @Inject constructor(
    private val passcodeRepository: PasscodeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private var _uiState: MutableState<UiState> = mutableStateOf(UiState())
    val uiState: State<UiState> = _uiState

    fun onUserInteraction(userInteraction: UserInteraction) {
        when (userInteraction) {
            is UserInteraction.OnPasscodeChanged -> onPasscodeChanged(userInteraction.newPasscode)
        }
    }

    private fun onPasscodeChanged(newPasscode: String) {
        if (newPasscode.length <= MAX_PASSCODE_SIZE) {
            updateUiState { it.copy(passcode = newPasscode, error = null) }
            if (newPasscode.length == MAX_PASSCODE_SIZE) {
                sendPasscode()
            }
        }
    }

    private fun sendPasscode() {
        viewModelScope.launch {
            when (
                val apiResult = passcodeRepository.finalizePasscodeLogin(uiState.value.passcode)
            ) {
                is ApiResult.HttpError -> showError("finalizePasscodeLogin: ${apiResult.msg}")
                ApiResult.NetworkError -> showError("Network error!")
                is ApiResult.Success -> checkForCredentials()
                is ApiResult.GeneralError -> showError("finalizePasscodeLogin: ${apiResult.msg}")
            }
        }
    }

    private suspend fun checkForCredentials() {
        when (val apiResult = userRepository.getUserById()) {
            is ApiResult.HttpError -> showError("getUserById: ${apiResult.msg}")
            ApiResult.NetworkError -> showError("Network error!")
            is ApiResult.Success -> {
                if (apiResult.data.webauthnCredentials.isNullOrEmpty()) {
                    updateUiState { it.copy(goToCreatePasskeyScreen = true) }
                } else {
                    updateUiState { it.copy(goToHomeScreen = true) }
                }
            }

            is ApiResult.GeneralError -> showError("getUserById: ${apiResult.msg}")
        }
    }

    private fun updateUiState(updateFunc: (UiState) -> UiState) {
        _uiState.value = updateFunc(uiState.value)
    }

    private fun showError(text: String) {
        updateUiState { it.copy(error = text) }
    }

    data class UiState(
        val passcode: String = "",
        val error: String? = null,
        val goToCreatePasskeyScreen: Boolean = false,
        val goToHomeScreen: Boolean = false,
    )

    sealed interface UserInteraction {
        data class OnPasscodeChanged(val newPasscode: String) : UserInteraction
    }

    companion object {
        const val MAX_PASSCODE_SIZE = 6
    }
}

