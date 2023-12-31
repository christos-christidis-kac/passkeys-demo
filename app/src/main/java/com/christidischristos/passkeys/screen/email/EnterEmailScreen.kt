package com.christidischristos.passkeys.screen.email

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.PublicKeyCredential
import androidx.hilt.navigation.compose.hiltViewModel
import com.christidischristos.passkeys.R
import com.christidischristos.passkeys.screen.util.ScreenUtil.findActivity
import com.christidischristos.passkeys.ui.composable.MyPrimaryButton
import com.christidischristos.passkeys.ui.theme.PasskeysTheme
import com.christidischristos.passkeys.screen.email.EnterEmailViewModel.UserInteraction as Event

@Composable
fun EnterEmailScreen(
    onGoToEnterPasscodeScreen: () -> Unit,
    onGoToHomeScreen: () -> Unit,
    viewModel: EnterEmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    val context = LocalContext.current

    LaunchedEffect(uiState.goToEnterPasscodeScreen) {
        if (uiState.goToEnterPasscodeScreen) {
            onGoToEnterPasscodeScreen()
        }
    }

    LaunchedEffect(uiState.goToHomeScreen) {
        if (uiState.goToHomeScreen) {
            onGoToHomeScreen()
        }
    }

    LaunchedEffect(uiState.getCredentialRequest) {
        uiState.getCredentialRequest?.let { request ->
            val credentialManager = CredentialManager.create(context)
            try {
                val credential = credentialManager.getCredential(
                    request,
                    context.findActivity()
                ).credential as PublicKeyCredential
                viewModel.onUserInteraction(Event.OnSignInWithPasskeySuccess(credential))
            } catch (e: Exception) {
                println(e)
                viewModel.onUserInteraction(Event.OnSignInWithPasskeyException(e))
            }
        }
    }

    EnterEmailScreenContent(
        uiState = uiState,
        onUserInteraction = viewModel::onUserInteraction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnterEmailScreenContent(
    uiState: EnterEmailViewModel.UiState,
    onUserInteraction: (EnterEmailViewModel.UserInteraction) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = uiState.email,
            placeholder = { Text(stringResource(R.string.enter_your_email)) },
            onValueChange = { onUserInteraction(Event.OnEmailChanged(it)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        MyPrimaryButton(
            onClick = { onUserInteraction(Event.OnContinueClicked) },
            text = stringResource(R.string.text_continue),
            enabled = uiState.emailEnabled
        )
        Spacer(Modifier.height(16.dp))
        MyPrimaryButton(
            onClick = { onUserInteraction(Event.OnSignInWithPasskeyClicked) },
            text = stringResource(R.string.sign_in_with_passkey),
            enabled = uiState.loginWithPasskeyEnabled
        )
        Spacer(Modifier.height(16.dp))
        Text(text = uiState.error ?: "", color = Color.Red)
    }
}

@Preview(showBackground = true)
@Composable
fun EnterEmailScreenPreview() {
    PasskeysTheme {
        EnterEmailScreenContent(
            uiState = EnterEmailViewModel.UiState(
                email = "foo@bar.com",
                emailEnabled = true,
                loginWithPasskeyEnabled = true,
                error = "Network error!"
            ),
            onUserInteraction = {}
        )
    }
}
