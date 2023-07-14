package com.christidischristidis.passkeys.navigation

sealed class Destination(val route: String) {

    object EnterEmail : Destination(route = "EnterEmail")
    object EnterPasscode : Destination(route = "EnterPasscode")
    object CreatePasskey: Destination(route = "CreatePasskey")
    object Home : Destination(route = "Home")
}
