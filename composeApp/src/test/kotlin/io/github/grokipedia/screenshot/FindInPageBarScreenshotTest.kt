package io.github.grokipedia.screenshot

import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.runtime.CompositionLocalProvider
import io.github.grokipedia.ui.FindInPageBar
import org.junit.Test

class FindInPageBarScreenshotTest : ScreenshotTestBase() {

    @Test
    fun findInPageBar_empty() {
        captureScreenshot("FindInPageBar_empty") {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                FindInPageBar(
                    query = "",
                    onQueryChange = {},
                    matchCount = 0,
                    currentMatch = 0,
                    onNext = {},
                    onPrevious = {},
                    onClose = {}
                )
            }
        }
    }

    @Test
    fun findInPageBar_noMatches() {
        captureScreenshot("FindInPageBar_noMatches") {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                FindInPageBar(
                    query = "nonexistent",
                    onQueryChange = {},
                    matchCount = 0,
                    currentMatch = 0,
                    onNext = {},
                    onPrevious = {},
                    onClose = {}
                )
            }
        }
    }

    @Test
    fun findInPageBar_withMatches() {
        captureScreenshot("FindInPageBar_withMatches") {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                FindInPageBar(
                    query = "Wikipedia",
                    onQueryChange = {},
                    matchCount = 5,
                    currentMatch = 2,
                    onNext = {},
                    onPrevious = {},
                    onClose = {}
                )
            }
        }
    }
}
