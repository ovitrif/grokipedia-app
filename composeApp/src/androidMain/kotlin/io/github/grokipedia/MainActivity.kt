package io.github.grokipedia

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.grokipedia.data.initDataStore
import io.github.grokipedia.util.initKeyboardManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Enable WebView debugging
        WebView.setWebContentsDebuggingEnabled(true)

        // Initialize DataStore
        initDataStore(this)

        // Initialize KeyboardManager
        initKeyboardManager(this)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
