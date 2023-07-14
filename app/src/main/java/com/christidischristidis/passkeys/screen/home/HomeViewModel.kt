package com.christidischristidis.passkeys.screen.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christidischristidis.passkeys.repository.ApiResult
import com.christidischristidis.passkeys.repository.user.UserRepository
import com.christidischristidis.passkeys.repository.webauthn.WebauthnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val webauthnRepository: WebauthnRepository
) : ViewModel() {

    private var _uiState: MutableState<UiState> = mutableStateOf(UiState())
    val uiState: State<UiState> = _uiState

    init {
        viewModelScope.launch {
            when (val apiResult = userRepository.getUserById()) {
                is ApiResult.HttpError -> showError("getUserById: ${apiResult.msg}")
                ApiResult.NetworkError -> showError("Network error!")
                is ApiResult.Success -> {
                    updateUiState {
                        it.copy(
                            userId = apiResult.data.userId,
                            credentials = apiResult.data.webauthnCredentials
                                ?.map { credential -> credential.id } ?: emptyList()
                        )
                    }
                }

                is ApiResult.GeneralError -> showError("getUserById: ${apiResult.msg}")
            }
        }
    }

    fun onUserInteraction(userInteraction: UserInteraction) {
        when (userInteraction) {
            is UserInteraction.OnCredentialDeleteClicked -> {
                onCredentialDeleteClicked(userInteraction.id)
            }

            UserInteraction.OnLogoutClicked -> onLogoutClicked()
        }
    }

    private fun onCredentialDeleteClicked(credentialId: String) {
        updateUiState { it.copy(error = null) }
        viewModelScope.launch {
            when (val apiResult = webauthnRepository.deleteWebauthnCredential(credentialId)) {
                is ApiResult.HttpError -> showError("deleteWebauthnCredential: ${apiResult.msg}")
                ApiResult.NetworkError -> showError("Network error!")
                is ApiResult.Success -> {
                    updateUiState {
                        it.copy(
                            credentials = it.credentials.filterNot { id -> id == credentialId }
                        )
                    }
                }

                is ApiResult.GeneralError -> showError("deleteWebauthnCredential: ${apiResult.msg}")
            }
        }
    }

    private fun onLogoutClicked() {
        updateUiState { it.copy(error = null) }
        viewModelScope.launch {
            userRepository.logout()
            updateUiState { it.copy(goToEnterEmailScreen = true) }
        }
    }

    private fun updateUiState(updateFunc: (UiState) -> UiState) {
        _uiState.value = updateFunc(uiState.value)
    }

    private fun showError(text: String) {
        updateUiState { it.copy(error = text) }
    }

    data class UiState(
        val userId: String? = null,
        val credentials: List<String> = emptyList(),
        val error: String? = null,
        val goToEnterEmailScreen: Boolean = false
    )

    sealed interface UserInteraction {
        data class OnCredentialDeleteClicked(val id: String) : UserInteraction
        object OnLogoutClicked : UserInteraction
    }
}
