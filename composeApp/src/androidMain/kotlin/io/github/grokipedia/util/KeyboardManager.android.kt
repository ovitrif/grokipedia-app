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
            val imm = appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            println("[KEYBOARD] Toggled soft input")
        } catch (e: Exception) {
            println("[KEYBOARD] Failed to show keyboard: ${e.message}")
        }
    }
}
