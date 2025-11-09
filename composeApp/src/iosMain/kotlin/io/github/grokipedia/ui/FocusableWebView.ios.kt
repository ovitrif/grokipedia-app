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
    // iOS implementation - use standard WebView for now
    WebView(
        state = state,
        navigator = navigator,
        modifier = modifier,
        captureBackPresses = captureBackPresses
    )
}
