package com.christidischristos.passkeys.screen.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast

object ScreenUtil {

    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun Context.findActivity(): Activity {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        error("No activity found!")
    }
}
