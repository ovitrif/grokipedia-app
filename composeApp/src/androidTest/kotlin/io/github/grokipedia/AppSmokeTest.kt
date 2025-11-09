package io.github.grokipedia

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test to verify the app launches and WebView is displayed.
 *
 * This test validates:
 * - The app launches without crashing
 * - The WebView container is rendered and visible
 */
@RunWith(AndroidJUnit4::class)
class AppSmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunches_andWebViewIsDisplayed() {
        // Wait for the WebView to be displayed
        composeTestRule
            .onNodeWithTag("webview")
            .assertIsDisplayed()
    }
}
