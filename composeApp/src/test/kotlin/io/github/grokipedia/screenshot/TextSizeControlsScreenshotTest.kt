package io.github.grokipedia.screenshot

import io.github.grokipedia.LightColorScheme
import io.github.grokipedia.ui.TextSizeControls
import org.junit.Test

class TextSizeControlsScreenshotTest : ScreenshotTestBase() {

    @Test
    fun textSizeControls_default() {
        captureScreenshot("TextSizeControls_default") {
            TextSizeControls(
                currentPercent = 100,
                onDecrease = {},
                onIncrease = {}
            )
        }
    }

    @Test
    fun textSizeControls_min() {
        captureScreenshot("TextSizeControls_min") {
            TextSizeControls(
                currentPercent = 80,
                onDecrease = {},
                onIncrease = {}
            )
        }
    }

    @Test
    fun textSizeControls_max() {
        captureScreenshot("TextSizeControls_max") {
            TextSizeControls(
                currentPercent = 160,
                onDecrease = {},
                onIncrease = {}
            )
        }
    }

    @Test
    fun textSizeControls_light() {
        captureScreenshot("TextSizeControls_light", colorScheme = LightColorScheme) {
            TextSizeControls(
                currentPercent = 100,
                onDecrease = {},
                onIncrease = {}
            )
        }
    }
}
