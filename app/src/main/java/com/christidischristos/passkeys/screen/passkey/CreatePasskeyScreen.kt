package com.christidischristos.passkeys.screen.passkey

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.christidischristos.passkeys.R
import com.christidischristos.passkeys.screen.util.ScreenUtil.showToast
import com.christidischristos.passkeys.ui.composable.MyErrorText
import com.christidischristos.passkeys.ui.composable.MyPrimaryButton
import com.christidischristos.passkeys.ui.composable.MyTextButton
import com.christidischristos.passkeys.ui.theme.PasskeysTheme
import com.google.android.gms.fido.Fido
import com.christidischristos.passkeys.screen.passkey.CreatePasskeyViewModel.UserInteraction as Event

@Composable
fun CreatePasskeyScreen(
    onGoToHomeScreen: () -> Unit,
    viewModel: CreatePasskeyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        viewModel.onUserInteraction(Event.OnActivityResultReceived(it))
    }

    LaunchedEffect(uiState.goToHomeScreen) {
        if (uiState.goToHomeScreen) {
            onGoToHomeScreen()
        }
    }

    LaunchedEffect(uiState.optionsForIntent) {
        uiState.optionsForIntent?.let { options ->
            val fidoClient = Fido.getFido2ApiClient(context)
            fidoClient.getRegisterPendingIntent(options)
                .addOnFailureListener {
                    showToast(context, "register passkey failed; $it")
                }
                .addOnSuccessListener {
                    launcher.launch(IntentSenderRequest.Builder(it).build())
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
