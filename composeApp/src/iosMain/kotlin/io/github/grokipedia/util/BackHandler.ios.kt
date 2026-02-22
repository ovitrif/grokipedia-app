package io.github.grokipedia.util

import androidx.compose.runtime.Composable

/**
 * iOS implementation of back button handler
 * iOS doesn't have a system back button like Android, so this is a no-op
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op for iOS - iOS uses swipe gestures and navigation controllers
    // The WebView's own navigation is handled differently
}
