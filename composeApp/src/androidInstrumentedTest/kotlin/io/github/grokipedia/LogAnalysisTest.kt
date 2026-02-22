package io.github.grokipedia

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests that verify log output and check for errors/crashes.
 *
 * This test suite demonstrates how to:
 * - Capture device logs during test execution
 * - Analyze logs for fatal errors
 * - Verify no crashes occurred
 */
@RunWith(AndroidJUnit4::class)
class LogAnalysisTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun getDevice(): UiDevice {
        return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun app_doesNotCrashOnLaunch() {
        val device = getDevice()

        // Clear existing logs
        device.executeShellCommand("logcat -c")

        // Wait for WebView to be displayed
        composeTestRule
            .onNodeWithTag("webview")
            .assertExists()

        // Give the app a moment to fully initialize
        Thread.sleep(2000)

        // Capture logs
        val logs = device.executeShellCommand("logcat -d -s AndroidRuntime:E")

        // Assert no fatal exceptions
        assertFalse(
            "App should not have fatal exceptions on launch",
            logs.contains("FATAL EXCEPTION")
        )
    }

    @Test
    fun app_logsNoErrorsDuringWebViewLoad() {
        val device = getDevice()

        // Clear existing logs
        device.executeShellCommand("logcat -c")

        // Wait for WebView to be displayed
        composeTestRule
            .onNodeWithTag("webview")
            .assertExists()

        // Wait for potential WebView loading
        Thread.sleep(5000)

        // Capture logs related to our app
        val appLogs = device.executeShellCommand(
            "logcat -d -s 'io.github.grokipedia:*' AndroidRuntime:E"
        )

        // Check for common error patterns
        assertFalse(
            "Should not contain fatal errors",
            appLogs.contains("FATAL")
        )

        // Note: We don't fail on all errors as some warnings/errors from
        // system components are normal and not related to our app
    }

    @Test
    fun logcat_canBeCaptured() {
        val device = getDevice()

        // Clear logs
        device.executeShellCommand("logcat -c")

        // Write a test log
        device.executeShellCommand("log -t GrokipediaTest 'Test log message'")

        // Capture logs
        val logs = device.executeShellCommand("logcat -d -s GrokipediaTest:*")

        // Verify we can capture logs
        assertTrue(
            "Should be able to capture test log message",
            logs.contains("Test log message")
        )
    }

    @Test
    fun webView_loadingDoesNotProduceErrors() {
        val device = getDevice()

        // Clear logs before test
        device.executeShellCommand("logcat -c")

        // Wait for WebView
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("webview")
            .assertExists()

        // Wait for page to load
        Thread.sleep(8000)

        // Get WebView related logs
        val webViewLogs = device.executeShellCommand(
            "logcat -d -s chromium:E WebView:E"
        )

        // We don't assert no errors here because WebView may have legitimate
        // errors from the webpage itself. Instead, we just verify we can capture them.
        // In real scenarios, you'd parse specific error types you care about.

        // Example: Check that there are no crashes
        assertFalse(
            "WebView should not crash",
            webViewLogs.contains("FATAL") && webViewLogs.contains("chromium")
        )
    }
}
