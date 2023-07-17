package com.christidischristos.passkeys.screen.util

import android.content.Context
import android.widget.Toast

object ScreenUtil {

    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}
