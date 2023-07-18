package com.christidischristos.passkeys.screen.email

import android.util.Patterns
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.GetCredentialRequest
import androidx.credentials.PublicKeyCredential
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christidischristos.passkeys.datastore.Constants.USER_ID_KEY
import com.christidischristos.passkeys.repository.ApiResult
import com.christidischristos.passkeys.repository.passcode.PasscodeRepository
import com.christidischristos.passkeys.repository.user.UserRepository
import com.christidischristos.passkeys.repository.webauthn.WebauthnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnterEmailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val passcodeRepository: PasscodeRepository,
    private val webauthnRepository: WebauthnRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private var _uiState: MutableState<UiState> = mutableStateOf(UiState())
    val uiState: State<UiState> = _uiState

    fun onUserInteraction(userInteraction: UserInteraction) {
        when (userInteraction) {
            is UserInteraction.OnEmailChanged -> onEmailChanged(userInteraction.newEmail)
            UserInteraction.OnContinueClicked -> onContinueClicked()
            UserInteraction.OnSignInWithPasskeyClicked -> onSignInWithPasskeyClicked()
            is UserInteraction.OnSignInWithPasskeyException -> {
                onSignInWithPasskeyException(userInteraction.e)
            }

            is UserInteraction.OnSignInWithPasskeySuccess -> {
                onSignInWithPasskeySuccess(userInteraction.credential)
            }
        }
    }

    private fun onEmailChanged(newEmail: String) {
        val isEmail = Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()
        updateUiState {
            it.copy(
                email = newEmail,
                emailEnabled = isEmail,
                loginWithPasskeyEnabled = isEmail
            )
        }
    }

    private fun onContinueClicked() {
        updateUiState { it.copy(error = null) }
        getOrCreateUserAndContinue(withPasskey = false)
    }

    private fun onSignInWithPasskeyClicked() {
        updateUiState { it.copy(error = null) }
        getOrCreateUserAndContinue(withPasskey = true)
    }

    private fun onSignInWithPasskeySuccess(credential: PublicKeyCredential) {
        viewModelScope.launch {
            when (
                val apiResult = webauthnRepository.finalizeWebauthnLogin(credential)
            ) {
                is ApiResult.HttpError -> showError("finalizeWebauthnLogin: ${apiResult.msg}")
                ApiResult.NetworkError -> showError("Network error!")
                is ApiResult.Success -> updateUiState { it.copy(goToHomeScreen = true) }
                is ApiResult.GeneralError -> showError("finalizeWebauthnLogin: ${apiResult.msg}")
            }
        }
    }

    private fun onSignInWithPasskeyException(exception: Exception) {
        showError("Sign in w Passkey: ${exception.message}")
    }

    private fun getOrCreateUserAndContinue(withPasskey: Boolean) {
        viewModelScope.launch {
            when (val apiResult = userRepository.getUserDetailsByEmail(uiState.value.email)) {
                is ApiResult.HttpError -> {
                    if (apiResult.code == 404) { // FIXME
                        createUser()
                    } else {
                        showError("getUserDetailsByEmail: ${apiResult.msg}")
                    }
                }

                ApiResult.NetworkError -> showError("Network error!")

                is ApiResult.Success -> {
                    if (withPasskey) {
                        initWebauthnLogin(apiResult.data.userId)
                    } else {
                        initPasscodeLogin(
                            apiResult.data.userId,
                            apiResult.data.emailId
                        )
                    }
                }

                is ApiResult.GeneralError -> showError("getUserDetailsByEmail: ${apiResult.msg}")
            }
        }
    }

    private suspend fun createUser() {
        when (val apiResult = userRepository.createUser(uiState.value.email)) {
            is ApiResult.HttpError -> showError("createUser: ${apiResult.msg}")
            ApiResult.NetworkError -> showError("Network error!")
            is ApiResult.Success -> {
                initPasscodeLogin(
                    apiResult.data.userId,
                    apiResult.data.emailId
                )
            }

            is ApiResult.GeneralError -> showError("createUser: ${apiResult.msg}")
        }
    }

    private suspend fun initWebauthnLogin(userId: String) {
        when (val apiResult = webauthnRepository.initWebauthnLogin(userId)) {
            is ApiResult.HttpError -> showError("initWebauthnLogin: ${apiResult.msg}")
            ApiResult.NetworkError -> showError("Network error!")
            is ApiResult.Success -> {
                dataStore.edit { prefs ->
                    prefs[USER_ID_KEY] = userId
                }
                val getCredentialRequest = GetCredentialRequest(listOf(apiResult.data))
                updateUiState { it.copy(getCredentialRequest = getCredentialRequest) }
            }

            is ApiResult.GeneralError -> showError("initWebauthnLogin: ${apiResult.msg}")
        }
    }

    private suspend fun initPasscodeLogin(userId: String, emailId: String) {
        when (val apiResult = passcodeRepository.initPasscodeLogin(userId, emailId)) {
            is ApiResult.HttpError -> showError("initPasscodeLogin: ${apiResult.msg}")
            ApiResult.NetworkError -> showError("Network error!")
            is ApiResult.Success -> updateUiState { it.copy(goToEnterPasscodeScreen = true) }
            is ApiResult.GeneralError -> showError("initPasscodeLogin: ${apiResult.msg}")
        }
    }

    private fun updateUiState(updateFunc: (UiState) -> UiState) {
        _uiState.value = updateFunc(uiState.value)
    }

    private fun showError(text: String) {
        updateUiState { it.copy(error = text) }
    }

    data class UiState(
        val email: String = "",
        val emailEnabled: Boolean = false,
        val loginWithPasskeyEnabled: Boolean = false,
        val error: String? = null,
        val getCredentialRequest: GetCredentialRequest? = null,
        val goToEnterPasscodeScreen: Boolean = false,
        val goToHomeScreen: Boolean = false
    )

    sealed interface UserInteraction {
        data class OnEmailChanged(val newEmail: String) : UserInteraction
        object OnContinueClicked : UserInteraction
        object OnSignInWithPasskeyClicked : UserInteraction
        data class OnSignInWithPasskeySuccess(val credential: PublicKeyCredential) : UserInteraction
        data class OnSignInWithPasskeyException(val e: Exception) : UserInteraction
    }
}
