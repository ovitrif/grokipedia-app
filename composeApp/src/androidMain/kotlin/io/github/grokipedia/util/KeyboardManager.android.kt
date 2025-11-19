package io.github.grokipedia.util

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable

private lateinit var appContext: Context

fun initKeyboardManager(context: Context) {
    appContext = context.applicationContext
}

actual class KeyboardManager {
    actual fun showKeyboard() {
        try {
            // On modern Android (API 24+), the keyboard automatically appears when an input
            // element is focused. Since the JavaScript in App.kt already focuses the search
            // input, the system will automatically show the keyboard.
            //
            // Note: The deprecated toggleSoftInput(SHOW_FORCED, 0) has been removed.
            // The modern replacement showSoftInput() requires a View parameter, which we
            // don't have access to from the application context. Since the WebView input
            // is already focused via JavaScript, the keyboard will appear automatically.
            println("[KEYBOARD] Input focused, keyboard should appear automatically")
        } catch (e: Exception) {
            println("[KEYBOARD] Error: ${e.message}")
        }
    }
}
