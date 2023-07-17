package com.christidischristos.passkeys.screen.passcode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.christidischristos.passkeys.R
import com.christidischristos.passkeys.ui.theme.PasskeysTheme
import com.christidischristos.passkeys.screen.passcode.EnterPasscodeViewModel.UserInteraction as Event

@Composable
fun EnterPasscodeScreen(
    onGoToCreatePasskeyScreen: () -> Unit,
    onGoToHomeScreen: () -> Unit,
    viewModel: EnterPasscodeViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState

    LaunchedEffect(uiState.goToCreatePasskeyScreen) {
        if (uiState.goToCreatePasskeyScreen) {
            onGoToCreatePasskeyScreen()
        }
    }

    LaunchedEffect(uiState.goToHomeScreen) {
        if (uiState.goToHomeScreen) {
            onGoToHomeScreen()
        }
    }

    EnterPasscodeScreenContent(
        uiState = uiState,
        onUserInteraction = viewModel::onUserInteraction
    )
}

@Composable
fun EnterPasscodeScreenContent(
    uiState: EnterPasscodeViewModel.UiState,
    onUserInteraction: (EnterPasscodeViewModel.UserInteraction) -> Unit
) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.please_enter_your_passcode),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(16.dp))
            PasscodeBoxes(
                passcode = uiState.passcode,
                onValueChange = { onUserInteraction(Event.OnPasscodeChanged(it)) }
            )
            Spacer(Modifier.height(16.dp))
            Text(text = uiState.error ?: "", color = Color.Red)
        }
    }
}

@Preview
@Composable
fun EnterPasscodeScreenPreview() {
    PasskeysTheme {
        EnterPasscodeScreenContent(
            uiState = EnterPasscodeViewModel.UiState(
                passcode = "1234",
                error = "Network error!"
            ),
            onUserInteraction = {})
    }
}
