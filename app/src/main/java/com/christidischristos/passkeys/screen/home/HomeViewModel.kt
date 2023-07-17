package com.christidischristos.passkeys.screen.home

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christidischristos.passkeys.R
import com.christidischristos.passkeys.repository.ApiResult
import com.christidischristos.passkeys.repository.user.UserRepository
import com.christidischristos.passkeys.repository.webauthn.WebauthnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val webauthnRepository: WebauthnRepository,
    @ApplicationContext private val context: Context
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
                            welcomeString = getWelcomeString(apiResult.data.email),
                            credentialIds = apiResult.data.webauthnCredentials
                                ?.map { credential -> credential.id } ?: emptyList()
                        )
                    }
                }

                is ApiResult.GeneralError -> showError("getUserById: ${apiResult.msg}")
            }
        }
    }

    private fun getWelcomeString(email: String): AnnotatedString {
        return buildAnnotatedString {
            val text = context.getString(R.string.welcome_back, email)
            append(text)
            val startIndex = text.indexOf(email)
            val endIndex = startIndex + email.length
            addStyle(
                SpanStyle(fontWeight = FontWeight.Bold),
                start = 0,
                end = startIndex - 1
            )
            addStyle(
                SpanStyle(fontWeight = FontWeight.Bold, color = Color.Blue),
                start = startIndex,
                end = endIndex
            )
        }
    }

    fun onUserInteraction(userInteraction: UserInteraction) {
        when (userInteraction) {
            is UserInteraction.OnCredentialDeleteClicked -> {
                onCredentialDeleteClicked(userInteraction.credentialId)
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
                    println("previously: ${uiState.value.credentialIds}")
                    println("to be deleted: $credentialId")
                    updateUiState {
                        it.copy(
                            credentialIds = it.credentialIds.filterNot { id -> id == credentialId }
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
        val welcomeString: AnnotatedString? = null,
        val credentialIds: List<String> = emptyList(),
        val error: String? = null,
        val goToEnterEmailScreen: Boolean = false
    )

    sealed interface UserInteraction {
        data class OnCredentialDeleteClicked(val credentialId: String) : UserInteraction
        object OnLogoutClicked : UserInteraction
    }
}
