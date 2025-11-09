package io.github.grokipedia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Main app composable that displays the Grokipedia website in a WebView
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        val webViewState = rememberWebViewState("https://grokipedia.org")
        val navigator = rememberWebViewNavigator()

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            WebView(
                state = webViewState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("webview"),
                navigator = navigator,
                captureBackPresses = true
            )

            // Show loading indicator while the page is loading
            if (webViewState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}