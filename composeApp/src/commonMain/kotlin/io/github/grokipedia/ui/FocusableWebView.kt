package io.github.grokipedia.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.WebViewState

/**
 * Platform-specific WebView that properly handles keyboard focus
 */
@Composable
expect fun FocusableWebView(
    state: WebViewState,
    navigator: WebViewNavigator,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    onWebViewReady: () -> Unit = {},
)
