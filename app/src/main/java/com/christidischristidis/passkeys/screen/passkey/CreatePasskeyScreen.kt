package com.christidischristidis.passkeys.screen.passkey

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.christidischristidis.passkeys.R
import com.christidischristidis.passkeys.ui.theme.PasskeysTheme
import com.christidischristidis.passkeys.screen.passkey.CreatePasskeyViewModel.UserInteraction as Event

@Composable
fun CreatePasskeyScreen(
    onGoToHomeScreen: () -> Unit,
    viewModel: CreatePasskeyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    LaunchedEffect(uiState.goToHomeScreen) {
        if (uiState.goToHomeScreen) {
            onGoToHomeScreen()
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

    val activity = LocalContext.current.findActivity()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(
            onClick = { onUserInteraction(Event.OnCreatePasskeyClicked(activity)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = stringResource(R.string.create_passkey))
        }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = { onUserInteraction(Event.OnSkipClicked) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = stringResource(R.string.skip))
        }
        Spacer(Modifier.height(16.dp))
        Text(text = uiState.error ?: "", color = Color.Red)
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

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}
