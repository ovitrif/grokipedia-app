package io.github.grokipedia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.rememberWebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import io.github.grokipedia.data.SavedPagesRepository
import io.github.grokipedia.data.createDataStore
import io.github.grokipedia.screens.SavedPagesScreen
import io.github.grokipedia.ui.FocusableWebView
import io.github.grokipedia.util.KeyboardManager
import io.github.grokipedia.util.PlatformBackHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class Screen {
    WebView, SavedPages
}

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3),
    background = Color(0xFF141414),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

/**
 * Main app composable that displays the Grokipedia website in a WebView
 */
@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = DarkColorScheme) {
        var currentScreen by remember { mutableStateOf(Screen.WebView) }
        val repository = remember { SavedPagesRepository(createDataStore()) }

        when (currentScreen) {
            Screen.WebView -> WebViewScreen(
                repository = repository,
                onNavigateToSavedPages = { currentScreen = Screen.SavedPages }
            )

            Screen.SavedPages -> SavedPagesScreen(
                repository = repository,
                onNavigateBack = { currentScreen = Screen.WebView },
                onPageClick = { url ->
                    currentScreen = Screen.WebView
                    // URL will be loaded in WebViewScreen
                }
            )
        }
    }
}

@Composable
fun WebViewScreen(
    repository: SavedPagesRepository,
    onNavigateToSavedPages: () -> Unit
) {
    val webViewState = rememberWebViewState("https://grokipedia.com/")
    val navigator = rememberWebViewNavigator()
    val hazeState = remember { HazeState() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var menuExpanded by remember { mutableStateOf(false) }
    var isCurrentPageSaved by remember { mutableStateOf(false) }
    var hasAutoFocusedSearch by remember { mutableStateOf(false) }
    val keyboardManager = remember { KeyboardManager() }

    val isHomePage = webViewState.lastLoadedUrl?.contains("grokipedia.com") == true &&
            webViewState.lastLoadedUrl?.let { it == "https://grokipedia.com/" || it == "https://grokipedia.com" } == true

    // Check if current page is saved
    LaunchedEffect(webViewState.lastLoadedUrl) {
        webViewState.lastLoadedUrl?.let { url ->
            val savedPages = repository.savedPages.first()
            isCurrentPageSaved = savedPages.any { it.url == url }
        }
    }

    // Inject CSS to add top padding for topbar area (content scrolls behind transparent topbar)
    LaunchedEffect(webViewState.isLoading, webViewState.lastLoadedUrl) {
        if (!webViewState.isLoading) {
            val topbarOffsetCss = """
                (function() {
                    var style = document.createElement('style');
                    style.id = 'grokipedia-topbar-offset';
                    style.textContent = 'body { padding-top: 80px !important; }';
                    var existing = document.getElementById('grokipedia-topbar-offset');
                    if (existing) existing.remove();
                    document.head.appendChild(style);
                    console.log('[CSS] Injected topbar offset padding');
                })();
            """.trimIndent()
            navigator.evaluateJavaScript(topbarOffsetCss)
        }
    }

    // Auto-focus search input on homepage after page loads
    LaunchedEffect(webViewState.isLoading, isHomePage) {
        if (!webViewState.isLoading && isHomePage && !hasAutoFocusedSearch) {
            println("[FOCUS] Page loaded, will attempt focus in 2 seconds...")
            // Wait for page to fully render and be visible to user
            delay(2000)

            println("[FOCUS] Executing focus script...")
            // JavaScript to focus the search input with mobile keyboard support
            // Simplified focus script - platform handles viewport resize via imePadding()
            val focusScript = """
                (function() {
                    console.log('[FOCUS-JS] Starting focus attempt...');

                    function tryFocusSearch() {
                        console.log('[FOCUS-JS] Looking for search input...');

                        var selectors = [
                            'input[type="search"]',
                            'input[type="text"]',
                            'input[name*="search"]',
                            'input[placeholder*="Search"]',
                            'input.search',
                            '#search',
                            'input'
                        ];

                        var searchInput = null;
                        for (var i = 0; i < selectors.length; i++) {
                            searchInput = document.querySelector(selectors[i]);
                            if (searchInput) {
                                console.log('[FOCUS-JS] Found input with selector: ' + selectors[i]);
                                break;
                            }
                        }

                        if (!searchInput) {
                            console.log('[FOCUS-JS] No input found!');
                            return false;
                        }

                        console.log('[FOCUS-JS] Input found, attempting focus...');

                        // Remove readonly if present
                        searchInput.removeAttribute('readonly');

                        // Scroll into view - platform resize handles proper centering
                        searchInput.scrollIntoView({ behavior: 'smooth', block: 'center' });

                        // Focus and click
                        searchInput.focus();
                        searchInput.click();
                        console.log('[FOCUS-JS] Called focus() and click()');

                        // Verify focus after short delay
                        setTimeout(function() {
                            console.log('[FOCUS-JS] Active element:', document.activeElement.tagName);
                            console.log('[FOCUS-JS] Is focused:', document.activeElement === searchInput);
                        }, 200);

                        return true;
                    }

                    // Try immediately, retry if needed
                    if (!tryFocusSearch()) {
                        setTimeout(function() {
                            if (!tryFocusSearch()) {
                                setTimeout(tryFocusSearch, 1000);
                            }
                        }, 500);
                    }
                })();
            """.trimIndent()

            navigator.evaluateJavaScript(focusScript)
            println("[FOCUS] Script executed")

            // Show keyboard after JavaScript focuses the input
            delay(500)
            keyboardManager.showKeyboard()
            println("[FOCUS] Keyboard show requested")

            hasAutoFocusedSearch = true
        }

        // Reset flag when navigating away from homepage
        if (!isHomePage) {
            hasAutoFocusedSearch = false
        }
    }

    // Handle back button
    PlatformBackHandler(enabled = navigator.canGoBack) {
        navigator.navigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(Color(0xFF141414))
    ) {
        // WebView with insets so content doesn't render behind status bar
        FocusableWebView(
            state = webViewState,
            navigator = navigator,
            captureBackPresses = true,
            onWebViewReady = {
                // WebView is ready - will trigger focus logic
            },
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("webview")
        )

        // Bottom right navigation
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = (25.5).dp, bottom = 16.dp)
        ) {
            // Back button
            if (navigator.canGoBack) {
                FilledTonalIconButton(
                    onClick = { navigator.navigateBack() },
                    modifier = Modifier.size(28.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Home button
            FilledTonalIconButton(
                onClick = {
                    scope.launch {
                        navigator.loadUrl("https://grokipedia.com/")
                    }
                },
                modifier = Modifier.size(28.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(12.dp)
                )
            }

            // Save/Unsave button
            FilledTonalIconButton(
                onClick = {
                    scope.launch {
                        val url = webViewState.lastLoadedUrl ?: return@launch
                        val title = webViewState.pageTitle ?: url

                        if (isCurrentPageSaved) {
                            repository.removePage(url)
                            snackbarHostState.showSnackbar("Page removed")
                        } else {
                            repository.savePage(url, title)
                            snackbarHostState.showSnackbar("Page saved")
                        }

                        val savedPages = repository.savedPages.first()
                        isCurrentPageSaved = savedPages.any { it.url == url }
                    }
                },
                modifier = Modifier.size(28.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.StarOutline,
                    contentDescription = if (isCurrentPageSaved) "Unsave" else "Save",
                    modifier = Modifier.size(12.dp)
                )
            }

            // Saved Pages button
            FilledTonalIconButton(
                onClick = { onNavigateToSavedPages() },
                modifier = Modifier.size(28.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Bookmarks,
                    contentDescription = "Saved Pages",
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // Show loading indicator while the page is loading
        if (webViewState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Snackbar for notifications
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
