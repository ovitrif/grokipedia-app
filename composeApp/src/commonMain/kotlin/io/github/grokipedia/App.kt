package io.github.grokipedia

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import io.github.grokipedia.data.SavedPagesRepository
import io.github.grokipedia.data.createDataStore
import io.github.grokipedia.screens.SavedPagesScreen
import io.github.grokipedia.ui.FocusableWebView
import io.github.grokipedia.util.KeyboardManager
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
    
    // Auto-focus search input on homepage after page loads
    LaunchedEffect(webViewState.isLoading, isHomePage) {
        if (!webViewState.isLoading && isHomePage && !hasAutoFocusedSearch) {
            println("[FOCUS] Page loaded, will attempt focus in 2 seconds...")
            // Wait for page to fully render and be visible to user
            delay(2000)
            
            println("[FOCUS] Executing focus script...")
            // JavaScript to focus the search input with mobile keyboard support
            val focusScript = """
                (function() {
                    console.log('[FOCUS-JS] Starting focus attempt...');
                    
                    function tryFocusSearch() {
                        console.log('[FOCUS-JS] Looking for search input...');
                        
                        // Try multiple selectors
                        var selectors = [
                            'input[type="search"]',
                            'input[type="text"]',
                            'input[name*="search"]',
                            'input[name*="Search"]',
                            'input[placeholder*="Search"]',
                            'input[placeholder*="search"]',
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
                        console.log('[FOCUS-JS] Input tag:', searchInput.tagName);
                        console.log('[FOCUS-JS] Input type:', searchInput.type);
                        console.log('[FOCUS-JS] Input placeholder:', searchInput.placeholder);
                        
                        // Remove readonly if present
                        searchInput.removeAttribute('readonly');
                        
                        // Scroll into view
                        searchInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        
                        // Focus and click
                        searchInput.focus();
                        console.log('[FOCUS-JS] Called focus()');
                        
                        searchInput.click();
                        console.log('[FOCUS-JS] Called click()');
                        
                        // Try to trigger touch event
                        try {
                            var touchStart = new TouchEvent('touchstart', {
                                bubbles: true,
                                cancelable: true,
                                view: window,
                                touches: []
                            });
                            searchInput.dispatchEvent(touchStart);
                            console.log('[FOCUS-JS] Dispatched touch event');
                        } catch(e) {
                            console.log('[FOCUS-JS] Touch event failed:', e);
                        }
                        
                        // Try mouse event as fallback
                        var mouseDown = new MouseEvent('mousedown', {
                            bubbles: true,
                            cancelable: true,
                            view: window
                        });
                        searchInput.dispatchEvent(mouseDown);
                        console.log('[FOCUS-JS] Dispatched mouse event');
                        
                        // Final focus attempt with value test
                        setTimeout(function() {
                            searchInput.focus();
                            
                            // Set a temporary value to test if input is truly focused
                            var testValue = '';
                            searchInput.value = testValue;
                            
                            // Check if we can actually type
                            var inputEvent = new InputEvent('input', {
                                bubbles: true,
                                cancelable: true,
                                data: testValue
                            });
                            searchInput.dispatchEvent(inputEvent);
                            
                            console.log('[FOCUS-JS] Final focus attempt, value:', searchInput.value);
                            console.log('[FOCUS-JS] Active element:', document.activeElement.tagName);
                            console.log('[FOCUS-JS] Is focused:', document.activeElement === searchInput);
                            
                            // Listen for input to verify typing works
                            searchInput.addEventListener('input', function(e) {
                                console.log('[FOCUS-JS] Input received! Value:', searchInput.value);
                            });
                            
                            searchInput.addEventListener('keydown', function(e) {
                                console.log('[FOCUS-JS] Key down:', e.key);
                            });
                        }, 200);
                        
                        console.log('[FOCUS-JS] Focus complete');
                        return true;
                    }
                    
                    // Try immediately
                    if (!tryFocusSearch()) {
                        console.log('[FOCUS-JS] First attempt failed, retrying in 500ms...');
                        setTimeout(function() {
                            if (!tryFocusSearch()) {
                                console.log('[FOCUS-JS] Second attempt failed, retrying in 1000ms...');
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
    BackHandler {
        if (navigator.canGoBack) {
            navigator.navigateBack()
        }
    }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF141414))
        ) {
            // WebView with haze source and top padding for the topbar
            FocusableWebView(
                state = webViewState,
                navigator = navigator,
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 56.dp) // Reserve space for topbar
                    .testTag("webview"),
                captureBackPresses = true,
                onWebViewReady = {
                    // WebView is ready - will trigger focus logic
                }
            )

            // Transparent topbar with haze blur effect
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(56.dp)
                    .hazeChild(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = Color(0xFF141414),
                            blurRadius = 20.dp,
                            tint = HazeTint(color = Color(0xFF141414).copy(alpha = 0.7f))
                        )
                    )
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button (hidden on home page)
                if (!isHomePage && navigator.canGoBack) {
                    IconButton(
                        onClick = {
                            navigator.navigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                } else {
                    // Empty space to maintain layout
                    Spacer(modifier = Modifier.width(48.dp))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Dropdown menu button
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                    
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        // Home menu item
                        DropdownMenuItem(
                            text = { Text("Home") },
                            onClick = {
                                menuExpanded = false
                                scope.launch {
                                    navigator.loadUrl("https://grokipedia.com/")
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home"
                                )
                            }
                        )
                        
                        // Saved Pages menu item
                        DropdownMenuItem(
                            text = { Text("Saved Pages") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToSavedPages()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Bookmarks,
                                    contentDescription = "Saved Pages"
                                )
                            }
                        )
                        
                        // Save/Unsave page menu item
                        DropdownMenuItem(
                            text = { 
                                Text(if (isCurrentPageSaved) "Unsave Page" else "Save Page") 
                            },
                            onClick = {
                                menuExpanded = false
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
                                    
                                    // Update saved state
                                    val savedPages = repository.savedPages.first()
                                    isCurrentPageSaved = savedPages.any { it.url == url }
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isCurrentPageSaved) 
                                        Icons.Filled.Star 
                                    else 
                                        Icons.Outlined.StarOutline,
                                    contentDescription = if (isCurrentPageSaved) "Unsave" else "Save"
                                )
                            }
                        )
                    }
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
