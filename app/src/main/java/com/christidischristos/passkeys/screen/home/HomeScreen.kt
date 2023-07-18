package com.christidischristos.passkeys.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.christidischristos.passkeys.R
import com.christidischristos.passkeys.ui.theme.PasskeysTheme
import java.util.UUID
import com.christidischristos.passkeys.screen.home.HomeViewModel.UserInteraction as Event

@Composable
fun HomeScreen(
    onGoToHomeScreen: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    LaunchedEffect(uiState.goToEnterEmailScreen) {
        if (uiState.goToEnterEmailScreen) {
            onGoToHomeScreen()
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onUserInteraction = viewModel::onUserInteraction
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeViewModel.UiState,
    onUserInteraction: (HomeViewModel.UserInteraction) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = uiState.welcomeString ?: AnnotatedString(""),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(32.dp))
        FakeTransactions()
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.your_credentials_ids_are),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )
        uiState.credentialIds.forEach {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    it,
                    modifier = Modifier.weight(9f),
                    textAlign = TextAlign.Start
                )
                IconButton(
                    onClick = { onUserInteraction(Event.OnCredentialDeleteClicked(it)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null
                    )
                }
            }
            Divider(thickness = 1.dp)
        }
        if (uiState.credentialIds.isEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.you_have_no_credentials_sorry))
        }
        Spacer(Modifier.height(32.dp))
        OutlinedButton(
            onClick = { onUserInteraction(Event.OnLogoutClicked) },
            border = BorderStroke(1.dp, Color.Red),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = stringResource(R.string.logout),
                color = Color.Red
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val randomId = UUID.randomUUID().toString()
    PasskeysTheme {
        HomeScreenContent(
            uiState = HomeViewModel.UiState(
                welcomeString = AnnotatedString("Welcome back, foo@bar.com!"),
                credentialIds = listOf(randomId, randomId),
                error = "Network error!"
            ),
            onUserInteraction = {}
        )
    }
}
