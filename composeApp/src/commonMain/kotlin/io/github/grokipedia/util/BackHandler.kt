package io.github.grokipedia.util

import androidx.compose.runtime.Composable

/**
 * Platform-specific back button handler
 * @param enabled Whether the back handler is enabled
 * @param onBack Callback to invoke when back button is pressed
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)
