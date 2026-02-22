package io.github.grokipedia.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.WebViewState

@Composable
actual fun FocusableWebView(
    state: WebViewState,
    navigator: WebViewNavigator,
    modifier: Modifier,
    captureBackPresses: Boolean,
    onWebViewReady: () -> Unit
) {
    // Use the library-provided WebView to ensure state and navigator are wired correctly.
    // This keeps WebViewState.isLoading accurate so the spinner can disappear,
    // and lets App.kt run its auto-focus logic once loading finishes.
    WebView(
        state = state,
        navigator = navigator,
        modifier = modifier,
        captureBackPresses = captureBackPresses
    )
    // Notify when the WebView Composable is in place
    onWebViewReady()
}
