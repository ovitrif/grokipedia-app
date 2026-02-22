package io.github.grokipedia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Tab
import androidx.compose.material.icons.automirrored.outlined.Toc
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.rememberWebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import io.github.grokipedia.data.ReadingHistoryRepository
import io.github.grokipedia.data.SavedPagesRepository
import io.github.grokipedia.data.TabManager
import io.github.grokipedia.data.TabState
import io.github.grokipedia.data.UserPreferencesRepository
import io.github.grokipedia.data.createDataStore
import io.github.grokipedia.screens.HistoryScreen
import io.github.grokipedia.screens.SavedPagesScreen
import io.github.grokipedia.screens.SettingsScreen
import io.github.grokipedia.screens.TabSwitcherScreen
import io.github.grokipedia.ui.FindInPageBar
import io.github.grokipedia.ui.TableOfContentsSheet
import io.github.grokipedia.ui.TextSizeControls
import io.github.grokipedia.ui.TocItem
import io.github.grokipedia.ui.FocusableWebView
import io.github.grokipedia.util.KeyboardManager
import io.github.grokipedia.util.PlatformBackHandler
import io.github.grokipedia.util.ShareManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class Screen {
    WebView, SavedPages, History, Settings, TabSwitcher
}

// Theme color schemes
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

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

val SepiaColorScheme = darkColorScheme(
    primary = Color(0xFF8B6914),
    secondary = Color(0xFF6B4E16),
    background = Color(0xFFF4ECD8),
    surface = Color(0xFFE8DCC8),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF5B4636),
    onSurface = Color(0xFF5B4636)
)

val BlackColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

data class ThemeCss(
    val bodyBg: String,
    val bodyColor: String,
    val imgFilter: String = ""
)

val themeCssMap = mapOf(
    "dark" to ThemeCss("#141414", "#E0E0E0", "img { filter: brightness(0.8); }"),
    "light" to ThemeCss("#FFFFFF", "#000000"),
    "sepia" to ThemeCss("#F4ECD8", "#5B4636"),
    "black" to ThemeCss("#000000", "#FFFFFF", "img { filter: brightness(0.8); }")
)

fun colorSchemeForTheme(theme: String) = when (theme) {
    "light" -> LightColorScheme
    "sepia" -> SepiaColorScheme
    "black" -> BlackColorScheme
    else -> DarkColorScheme
}

/**
 * Main app composable that displays the Grokipedia website in a WebView
 */
@Composable
@Preview
fun App() {
    val dataStore = remember { createDataStore() }
    val savedPagesRepository = remember { SavedPagesRepository(dataStore) }
    val historyRepository = remember { ReadingHistoryRepository(dataStore) }
    val userPreferencesRepository = remember { UserPreferencesRepository(dataStore) }
    val tabManager = remember { TabManager(dataStore) }

    val currentTheme by userPreferencesRepository.theme.collectAsState(initial = "dark")

    MaterialTheme(colorScheme = colorSchemeForTheme(currentTheme)) {
        var currentScreen by remember { mutableStateOf(Screen.WebView) }
        var pendingUrl by remember { mutableStateOf<String?>(null) }

        // Handle system back for non-WebView screens
        PlatformBackHandler(enabled = currentScreen != Screen.WebView) {
            currentScreen = Screen.WebView
        }

        when (currentScreen) {
            Screen.WebView -> WebViewScreen(
                savedPagesRepository = savedPagesRepository,
                historyRepository = historyRepository,
                userPreferencesRepository = userPreferencesRepository,
                tabManager = tabManager,
                pendingUrl = pendingUrl,
                onPendingUrlConsumed = { pendingUrl = null },
                onNavigateToSavedPages = { currentScreen = Screen.SavedPages },
                onNavigateToHistory = { currentScreen = Screen.History },
                onNavigateToSettings = { currentScreen = Screen.Settings },
                onNavigateToTabSwitcher = { currentScreen = Screen.TabSwitcher }
            )

            Screen.SavedPages -> SavedPagesScreen(
                repository = savedPagesRepository,
                onNavigateBack = { currentScreen = Screen.WebView },
                onPageClick = { url ->
                    pendingUrl = url
                    currentScreen = Screen.WebView
                }
            )

            Screen.History -> HistoryScreen(
                repository = historyRepository,
                onNavigateBack = { currentScreen = Screen.WebView },
                onPageClick = { url ->
                    pendingUrl = url
                    currentScreen = Screen.WebView
                }
            )

            Screen.Settings -> SettingsScreen(
                userPreferencesRepository = userPreferencesRepository,
                historyRepository = historyRepository,
                savedPagesRepository = savedPagesRepository,
                onNavigateBack = { currentScreen = Screen.WebView }
            )

            Screen.TabSwitcher -> TabSwitcherScreen(
                tabManager = tabManager,
                onNavigateBack = { currentScreen = Screen.WebView },
                onTabClick = { tabId ->
                    currentScreen = Screen.WebView
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    savedPagesRepository: SavedPagesRepository,
    historyRepository: ReadingHistoryRepository,
    userPreferencesRepository: UserPreferencesRepository,
    tabManager: TabManager,
    pendingUrl: String?,
    onPendingUrlConsumed: () -> Unit,
    onNavigateToSavedPages: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTabSwitcher: () -> Unit
) {
    val webViewState = rememberWebViewState("https://grokipedia.com/")
    val navigator = rememberWebViewNavigator()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isCurrentPageSaved by remember { mutableStateOf(false) }
    var hasAutoFocusedSearch by remember { mutableStateOf(false) }
    val keyboardManager = remember { KeyboardManager() }
    val shareManager = remember { ShareManager() }

    // User preferences
    val textSizePercent by userPreferencesRepository.textSizePercent.collectAsState(initial = 100)
    val autoFocusEnabled by userPreferencesRepository.autoFocusEnabled.collectAsState(initial = true)
    val currentTheme by userPreferencesRepository.theme.collectAsState(initial = "dark")

    // Tab state
    val tabState by tabManager.tabState.collectAsState(initial = TabState())

    // UI state
    var showMoreSheet by remember { mutableStateOf(false) }
    var showFindInPage by remember { mutableStateOf(false) }
    var showTocSheet by remember { mutableStateOf(false) }
    var showTextSizeControls by remember { mutableStateOf(false) }

    // Find in page state
    var findQuery by remember { mutableStateOf("") }
    var findMatchCount by remember { mutableStateOf(0) }
    var findCurrentMatch by remember { mutableStateOf(0) }

    // TOC state
    var tocItems by remember { mutableStateOf<List<TocItem>>(emptyList()) }

    val isHomePage = webViewState.lastLoadedUrl?.contains("grokipedia.com") == true &&
            webViewState.lastLoadedUrl?.let { it == "https://grokipedia.com/" || it == "https://grokipedia.com" } == true

    // Handle pending URL from navigation
    LaunchedEffect(pendingUrl) {
        pendingUrl?.let { url ->
            navigator.loadUrl(url)
            onPendingUrlConsumed()
        }
    }

    // Check if current page is saved
    LaunchedEffect(webViewState.lastLoadedUrl) {
        webViewState.lastLoadedUrl?.let { url ->
            val savedPages = savedPagesRepository.savedPages.first()
            isCurrentPageSaved = savedPages.any { it.url == url }
        }
    }

    // Track reading history
    LaunchedEffect(webViewState.lastLoadedUrl) {
        webViewState.lastLoadedUrl?.let { url ->
            if (url.isNotBlank() && url != "about:blank") {
                val title = webViewState.pageTitle ?: url
                historyRepository.addToHistory(url, title)
            }
        }
    }

    // Update tab info
    LaunchedEffect(webViewState.lastLoadedUrl, webViewState.pageTitle) {
        val url = webViewState.lastLoadedUrl ?: return@LaunchedEffect
        val title = webViewState.pageTitle ?: url
        val activeTabId = tabState.activeTabId
        tabManager.updateTabInfo(activeTabId, url, title)
    }

    // Inject CSS for topbar offset, text size, and theme after page load
    LaunchedEffect(webViewState.isLoading, webViewState.lastLoadedUrl, textSizePercent, currentTheme) {
        if (!webViewState.isLoading) {
            val theme = themeCssMap[currentTheme] ?: themeCssMap["dark"]!!
            val cssInjection = """
                (function() {
                    var style = document.createElement('style');
                    style.id = 'grokipedia-custom-styles';
                    style.textContent = 'body { padding-top: 80px !important; font-size: ${textSizePercent}% !important; background-color: ${theme.bodyBg} !important; color: ${theme.bodyColor} !important; } a { color: ${theme.bodyColor} !important; } ${theme.imgFilter}';
                    var existing = document.getElementById('grokipedia-custom-styles');
                    if (existing) existing.remove();
                    document.head.appendChild(style);
                    console.log('[CSS] Injected text size: ${textSizePercent}%, theme: $currentTheme');
                })();
            """.trimIndent()
            navigator.evaluateJavaScript(cssInjection)
        }
    }

    // Auto-focus search input on homepage after page loads
    LaunchedEffect(webViewState.isLoading, isHomePage, autoFocusEnabled) {
        if (!webViewState.isLoading && isHomePage && !hasAutoFocusedSearch && autoFocusEnabled) {
            println("[FOCUS] Page loaded, will attempt focus in 2 seconds...")
            delay(2000)

            println("[FOCUS] Executing focus script...")
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
                        searchInput.removeAttribute('readonly');
                        searchInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        searchInput.focus();
                        searchInput.click();
                        console.log('[FOCUS-JS] Called focus() and click()');

                        setTimeout(function() {
                            console.log('[FOCUS-JS] Active element:', document.activeElement.tagName);
                            console.log('[FOCUS-JS] Is focused:', document.activeElement === searchInput);
                        }, 200);

                        return true;
                    }

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

            delay(500)
            keyboardManager.showKeyboard()
            println("[FOCUS] Keyboard show requested")

            hasAutoFocusedSearch = true
        }

        if (!isHomePage) {
            hasAutoFocusedSearch = false
        }
    }

    // Handle back button
    PlatformBackHandler(enabled = navigator.canGoBack) {
        navigator.navigateBack()
    }

    val bgColor = when (currentTheme) {
        "light" -> Color(0xFFFFFFFF)
        "sepia" -> Color(0xFFF4ECD8)
        "black" -> Color(0xFF000000)
        else -> Color(0xFF141414)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(bgColor)
    ) {
        // WebView
        FocusableWebView(
            state = webViewState,
            navigator = navigator,
            captureBackPresses = true,
            onWebViewReady = {},
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("webview")
        )

        // Find in page bar at top
        if (showFindInPage) {
            FindInPageBar(
                query = findQuery,
                onQueryChange = { query ->
                    findQuery = query
                    if (query.isNotEmpty()) {
                        // Count matches and find first
                        val countScript = """
                            (function() {
                                var text = document.body.innerText;
                                var query = '${query.replace("'", "\\'")}';
                                var count = text.toLowerCase().split(query.toLowerCase()).length - 1;
                                window.find(query, false, false, true);
                                return '' + count;
                            })();
                        """.trimIndent()
                        scope.launch {
                            navigator.evaluateJavaScript(countScript) { result ->
                                val count = result?.replace("\"", "")?.toIntOrNull() ?: 0
                                findMatchCount = count
                                findCurrentMatch = if (count > 0) 1 else 0
                                println("[FIND] Query: '$query', matches: $count")
                            }
                        }
                    } else {
                        findMatchCount = 0
                        findCurrentMatch = 0
                    }
                },
                matchCount = findMatchCount,
                currentMatch = findCurrentMatch,
                onNext = {
                    scope.launch {
                        navigator.evaluateJavaScript("window.find('${findQuery.replace("'", "\\'")}', false, false, true);")
                        if (findCurrentMatch < findMatchCount) findCurrentMatch++
                        else findCurrentMatch = 1
                    }
                },
                onPrevious = {
                    scope.launch {
                        navigator.evaluateJavaScript("window.find('${findQuery.replace("'", "\\'")}', false, true, true);")
                        if (findCurrentMatch > 1) findCurrentMatch--
                        else findCurrentMatch = findMatchCount
                    }
                },
                onClose = {
                    showFindInPage = false
                    findQuery = ""
                    findMatchCount = 0
                    findCurrentMatch = 0
                    // Clear highlights
                    scope.launch {
                        navigator.evaluateJavaScript("window.getSelection().removeAllRanges();")
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // Text size controls overlay
        if (showTextSizeControls) {
            TextSizeControls(
                currentPercent = textSizePercent,
                onDecrease = {
                    scope.launch { userPreferencesRepository.setTextSize(textSizePercent - 20) }
                },
                onIncrease = {
                    scope.launch { userPreferencesRepository.setTextSize(textSizePercent + 20) }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 60.dp, bottom = 16.dp)
            )
        }

        // Bottom right navigation
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = (25.5).dp, bottom = 16.dp)
        ) {
            // More button
            FilledTonalIconButton(
                onClick = {
                    showMoreSheet = true
                    showTextSizeControls = false
                    println("[NAV] Opened more sheet")
                },
                modifier = Modifier.size(28.dp).testTag("more_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "More",
                    modifier = Modifier.size(12.dp)
                )
            }

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
                            savedPagesRepository.removePage(url)
                            snackbarHostState.showSnackbar("Page removed")
                        } else {
                            savedPagesRepository.savePage(url, title)
                            snackbarHostState.showSnackbar("Page saved")
                        }

                        val savedPages = savedPagesRepository.savedPages.first()
                        isCurrentPageSaved = savedPages.any { it.url == url }
                    }
                },
                modifier = Modifier.size(28.dp).testTag("save_button"),
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
                modifier = Modifier.size(28.dp).testTag("saved_pages_button"),
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

    // More overflow bottom sheet
    if (showMoreSheet) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1E1E1E),
            modifier = Modifier.testTag("more_sheet")
        ) {
            Column(
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                OverflowMenuItem(
                    icon = Icons.Outlined.Share,
                    label = "Share",
                    testTag = "more_share",
                    onClick = {
                        showMoreSheet = false
                        val url = webViewState.lastLoadedUrl ?: return@OverflowMenuItem
                        val title = webViewState.pageTitle ?: url
                        shareManager.share(url, title)
                        println("[NAV] Selected: Share")
                    }
                )

                OverflowMenuItem(
                    icon = Icons.Outlined.FormatSize,
                    label = "Text Size",
                    trailingText = "$textSizePercent%",
                    testTag = "more_text_size",
                    onClick = {
                        showMoreSheet = false
                        showTextSizeControls = !showTextSizeControls
                        println("[NAV] Selected: Text Size")
                    }
                )

                OverflowMenuItem(
                    icon = Icons.Outlined.FindInPage,
                    label = "Find in Page",
                    testTag = "more_find",
                    onClick = {
                        showMoreSheet = false
                        showFindInPage = true
                        println("[NAV] Selected: Find in Page")
                    }
                )

                OverflowMenuItem(
                    icon = Icons.AutoMirrored.Outlined.Toc,
                    label = "Table of Contents",
                    testTag = "more_toc",
                    onClick = {
                        showMoreSheet = false
                        // Extract TOC from page
                        scope.launch {
                            val tocScript = """
                                (function() {
                                    var headings = document.querySelectorAll('h1,h2,h3,h4,h5,h6');
                                    var result = [];
                                    headings.forEach(function(h, i) {
                                        if (!h.id) h.id = 'toc_heading_' + i;
                                        result.push({
                                            text: h.textContent.trim(),
                                            level: parseInt(h.tagName.charAt(1)),
                                            id: h.id
                                        });
                                    });
                                    return JSON.stringify(result);
                                })();
                            """.trimIndent()
                            navigator.evaluateJavaScript(tocScript) { result ->
                                try {
                                    val jsonStr = result?.trim()?.removeSurrounding("\"")
                                        ?.replace("\\\"", "\"")
                                        ?.replace("\\\\", "\\") ?: "[]"
                                    val jsonArray = Json.parseToJsonElement(jsonStr).jsonArray
                                    tocItems = jsonArray.map { element ->
                                        val obj = element.jsonObject
                                        TocItem(
                                            text = obj["text"]?.jsonPrimitive?.content ?: "",
                                            level = obj["level"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1,
                                            elementId = obj["id"]?.jsonPrimitive?.content ?: ""
                                        )
                                    }
                                    println("[TOC] Extracted ${tocItems.size} headings")
                                    if (tocItems.isNotEmpty()) {
                                        showTocSheet = true
                                    }
                                } catch (e: Exception) {
                                    println("[TOC] Parse error: ${e.message}")
                                }
                            }
                        }
                        println("[NAV] Selected: Table of Contents")
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = Color.Gray.copy(alpha = 0.2f)
                )

                OverflowMenuItem(
                    icon = Icons.Outlined.History,
                    label = "History",
                    testTag = "more_history",
                    onClick = {
                        showMoreSheet = false
                        onNavigateToHistory()
                        println("[NAV] Selected: History")
                    }
                )

                OverflowMenuItem(
                    icon = Icons.Outlined.Tab,
                    label = "Tabs",
                    trailingText = "${tabState.tabs.size}",
                    testTag = "more_tabs",
                    onClick = {
                        showMoreSheet = false
                        onNavigateToTabSwitcher()
                        println("[NAV] Selected: Tabs")
                    }
                )

                OverflowMenuItem(
                    icon = Icons.Outlined.Settings,
                    label = "Settings",
                    testTag = "more_settings",
                    onClick = {
                        showMoreSheet = false
                        onNavigateToSettings()
                        println("[NAV] Selected: Settings")
                    }
                )
            }
        }
    }

    // Table of contents bottom sheet
    if (showTocSheet && tocItems.isNotEmpty()) {
        TableOfContentsSheet(
            items = tocItems,
            onItemClick = { item ->
                showTocSheet = false
                scope.launch {
                    navigator.evaluateJavaScript(
                        "document.getElementById('${item.elementId}').scrollIntoView({behavior:'smooth'});"
                    )
                }
            },
            onDismiss = { showTocSheet = false }
        )
    }
}

@Composable
private fun OverflowMenuItem(
    icon: ImageVector,
    label: String,
    testTag: String,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
