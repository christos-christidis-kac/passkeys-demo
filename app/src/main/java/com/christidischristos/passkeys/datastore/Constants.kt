package com.christidischristos.passkeys.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
    val USER_ID_KEY = stringPreferencesKey("USER_ID_KEY")
    val PASSCODE_INIT_ID_KEY = stringPreferencesKey("PASSCODE_INIT_ID_KEY")
    val JWT_KEY = stringPreferencesKey("JWT_KEY")
}
