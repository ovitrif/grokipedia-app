package io.github.grokipedia.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.WebViewState

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun FocusableWebView(
    state: WebViewState,
    navigator: WebViewNavigator,
    modifier: Modifier,
    captureBackPresses: Boolean,
    onWebViewReady: () -> Unit
) {
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Enable JavaScript
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportMultipleWindows(false)

            // Enable touch and focus
            isFocusable = true
            isFocusableInTouchMode = true

            // Set WebViewClient
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Request focus when page finishes loading
                    view?.requestFocus()
                    println("[WebView] Page loaded: $url, requesting focus")
                }
            }

            // Load initial URL
            loadUrl(state.lastLoadedUrl ?: "https://grokipedia.com/")
        }
    }

    // Navigator is already integrated with the state
    // We just need to keep the WebView in sync

    DisposableEffect(Unit) {
        onWebViewReady()
        onDispose {
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier,
        update = { view ->
            // Request focus when the view updates
            view.post {
                view.requestFocus()
                println("[WebView] Requesting focus in update")
            }
        }
    )
}
