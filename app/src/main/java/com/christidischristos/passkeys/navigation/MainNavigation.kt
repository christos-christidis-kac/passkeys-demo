package com.christidischristos.passkeys.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.christidischristos.passkeys.screen.email.EnterEmailScreen
import com.christidischristos.passkeys.screen.home.HomeScreen
import com.christidischristos.passkeys.screen.passcode.EnterPasscodeScreen
import com.christidischristos.passkeys.screen.passkey.CreatePasskeyScreen

@Composable
fun MainNavigation(
    navController: NavHostController,
    padding: PaddingValues  // ignore
) {
    NavHost(
        navController = navController,
        startDestination = Destination.EnterEmail.route
    ) {
        composable(Destination.EnterEmail.route) {
            EnterEmailScreen(
                onGoToEnterPasscodeScreen = {
                    navController.navigate(Destination.EnterPasscode.route) {
                        popUpTo(Destination.EnterEmail.route) { inclusive = true }
                    }
                },
                onGoToHomeScreen = {
                    navController.navigate(Destination.Home.route) {
                        popUpTo(Destination.EnterEmail.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Destination.EnterPasscode.route) {
            EnterPasscodeScreen(
                onGoToCreatePasskeyScreen = {
                    navController.navigate(Destination.CreatePasskey.route) {
                        popUpTo(Destination.EnterPasscode.route) { inclusive = true }
                    }
                },
                onGoToHomeScreen = {
                    navController.navigate(Destination.Home.route) {
                        popUpTo(Destination.EnterPasscode.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Destination.CreatePasskey.route) {
            CreatePasskeyScreen(onGoToHomeScreen = {
                navController.navigate(Destination.Home.route) {
                    popUpTo(Destination.CreatePasskey.route) { inclusive = true }
                }
            })
        }
        composable(Destination.Home.route) {
            HomeScreen(onGoToHomeScreen = {
                navController.navigate(Destination.EnterEmail.route) {
                    popUpTo(Destination.Home.route) { inclusive = true }
                }
            })
        }
    }
}
