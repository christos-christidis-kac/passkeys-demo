package com.christidischristos.passkeys.screen.passkey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.christidischristos.passkeys.R
import com.christidischristos.passkeys.screen.util.ScreenUtil.findActivity
import com.christidischristos.passkeys.ui.composable.MyErrorText
import com.christidischristos.passkeys.ui.composable.MyPrimaryButton
import com.christidischristos.passkeys.ui.composable.MyTextButton
import com.christidischristos.passkeys.ui.theme.PasskeysTheme
import com.christidischristos.passkeys.screen.passkey.CreatePasskeyViewModel.UserInteraction as Event

@Composable
fun CreatePasskeyScreen(
    onGoToHomeScreen: () -> Unit,
    viewModel: CreatePasskeyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    val context = LocalContext.current

    LaunchedEffect(uiState.goToHomeScreen) {
        if (uiState.goToHomeScreen) {
            onGoToHomeScreen()
        }
    }

    LaunchedEffect(uiState.createKeyRequest) {
        uiState.createKeyRequest?.let { request ->
            val credentialManager = CredentialManager.create(context)
            try {
                val response = credentialManager.createCredential(request, context.findActivity())
                    as CreatePublicKeyCredentialResponse
                viewModel.onUserInteraction(Event.OnCreatePasskeySuccess(response))
            } catch (e: CreateCredentialException) {
                viewModel.onUserInteraction(Event.OnCreatePasskeyException(e))
            }
        }
    }

    CreatePasskeyScreenContent(
        uiState = uiState,
        onUserInteraction = viewModel::onUserInteraction
    )
}

@Composable
fun CreatePasskeyScreenContent(
    uiState: CreatePasskeyViewModel.UiState,
    onUserInteraction: (CreatePasskeyViewModel.UserInteraction) -> Unit
) {

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyPrimaryButton(
            onClick = { onUserInteraction(Event.OnCreatePasskeyClicked) },
            text = stringResource(R.string.create_passkey)
        )
        Spacer(Modifier.height(8.dp))
        MyTextButton(
            onClick = { onUserInteraction(Event.OnSkipClicked) },
            text = stringResource(R.string.skip)
        )
        Spacer(Modifier.height(16.dp))
        MyErrorText(text = uiState.error)
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePasskeyScreenPreview() {
    PasskeysTheme {
        CreatePasskeyScreenContent(
            uiState = CreatePasskeyViewModel.UiState(
                error = "Network error!"
            ),
            onUserInteraction = {}
        )
    }
}
