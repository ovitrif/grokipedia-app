package io.github.grokipedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import io.github.grokipedia.data.ReadingHistoryRepository
import io.github.grokipedia.data.SavedPagesRepository
import io.github.grokipedia.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ThemeOption(
    val id: String,
    val label: String,
    val backgroundColor: Color,
    val textColor: Color
)

val themeOptions = listOf(
    ThemeOption("dark", "Dark", Color(0xFF141414), Color(0xFFE0E0E0)),
    ThemeOption("light", "Light", Color(0xFFFFFFFF), Color(0xFF000000)),
    ThemeOption("sepia", "Sepia", Color(0xFFF4ECD8), Color(0xFF5B4636)),
    ThemeOption("black", "Black", Color(0xFF000000), Color(0xFFFFFFFF))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userPreferencesRepository: UserPreferencesRepository,
    historyRepository: ReadingHistoryRepository,
    savedPagesRepository: SavedPagesRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    val snackbarHostState = remember { SnackbarHostState() }

    val textSizePercent by userPreferencesRepository.textSizePercent.collectAsState(initial = 100)
    val autoFocusEnabled by userPreferencesRepository.autoFocusEnabled.collectAsState(initial = true)
    val currentTheme by userPreferencesRepository.theme.collectAsState(initial = "dark")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
            .testTag("settings_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 56.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Reading section
            item {
                SectionHeader("Reading")
            }

            item {
                // Text size
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("setting_text_size"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Text Size", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Text("$textSizePercent%", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = {
                                scope.launch { userPreferencesRepository.setTextSize(textSizePercent - 20) }
                            },
                            enabled = textSizePercent > 80,
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("A-")
                        }
                        FilledTonalButton(
                            onClick = {
                                scope.launch { userPreferencesRepository.setTextSize(textSizePercent + 20) }
                            },
                            enabled = textSizePercent < 160,
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("A+")
                        }
                    }
                }
            }

            item {
                // Auto-focus toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("setting_auto_focus"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-focus Search", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Text("Focus search input on homepage", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = autoFocusEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch { userPreferencesRepository.setAutoFocus(enabled) }
                        }
                    )
                }
            }

            // Appearance section
            item {
                SectionHeader("Appearance")
            }

            item {
                // Theme selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("Theme", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        themeOptions.forEach { theme ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        scope.launch { userPreferencesRepository.setTheme(theme.id) }
                                    }
                                    .testTag("theme_${theme.id}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(theme.backgroundColor)
                                        .then(
                                            if (currentTheme == theme.id) {
                                                Modifier.background(
                                                    color = Color.Transparent
                                                )
                                            } else Modifier
                                        )
                                ) {
                                    if (currentTheme == theme.id) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color.Transparent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.backgroundColor)
                                            )
                                        }
                                    }
                                    // Show text color sample
                                    Text(
                                        text = "A",
                                        color = theme.textColor,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = theme.label,
                                    color = if (currentTheme == theme.id) Color.White else Color.Gray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // Data section
            item {
                SectionHeader("Data")
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                historyRepository.clearHistory()
                                snackbarHostState.showSnackbar("History cleared")
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .testTag("setting_clear_history"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Clear Browsing History", color = Color(0xFFFF6B6B), style = MaterialTheme.typography.bodyLarge)
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                savedPagesRepository.clearAll()
                                snackbarHostState.showSnackbar("Saved pages cleared")
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .testTag("setting_clear_saved"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Clear Saved Pages", color = Color(0xFFFF6B6B), style = MaterialTheme.typography.bodyLarge)
                }
            }

            // About section
            item {
                SectionHeader("About")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("settings_about")
                ) {
                    Text("Grokipedia", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    Text("Version 1.0", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("grokipedia.com", color = Color(0xFFBB86FC), style = MaterialTheme.typography.bodySmall)
                }
            }

            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Topbar with haze blur
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(56.dp)
                .hazeEffect(
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
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFFBB86FC),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}
