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
    @Suppress("DEPRECATION")
    actual fun showKeyboard() {
        try {
            // Pragmatic fallback: explicitly request the IME to show.
            // We don't have a direct View reference here, so use toggleSoftInput as a
            // best-effort approach. This helps devices/keyboards that don't auto-show
            // on JS focus inside WebView.
            val imm = appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                println("[KEYBOARD] Requested IME show via toggleSoftInput")
            } else {
                println("[KEYBOARD] InputMethodManager not available")
            }
        } catch (e: Exception) {
            println("[KEYBOARD] Error: ${e.message}")
        }
    }
}
