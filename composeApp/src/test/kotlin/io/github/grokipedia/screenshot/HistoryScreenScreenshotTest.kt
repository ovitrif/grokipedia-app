package io.github.grokipedia.screenshot

import io.github.grokipedia.data.ReadingHistoryRepository
import io.github.grokipedia.screens.HistoryScreen
import kotlinx.coroutines.runBlocking
import org.junit.Test

class HistoryScreenScreenshotTest : ScreenshotTestBase() {

    @Test
    fun historyScreen_empty() {
        val dataStore = createTestDataStore()
        captureScreenshot("HistoryScreen_empty") {
            HistoryScreen(
                repository = ReadingHistoryRepository(dataStore),
                onNavigateBack = {},
                onPageClick = {}
            )
        }
    }

    @Test
    fun historyScreen_withItems() {
        val dataStore = createTestDataStore()
        val repository = ReadingHistoryRepository(dataStore)
        runBlocking {
            repository.addToHistory("https://grokipedia.com/wiki/Kotlin", "Kotlin (programming language)")
            repository.addToHistory("https://grokipedia.com/wiki/Compose", "Jetpack Compose")
            repository.addToHistory("https://grokipedia.com/wiki/Android", "Android (operating system)")
        }

        captureScreenshot("HistoryScreen_withItems") {
            HistoryScreen(
                repository = repository,
                onNavigateBack = {},
                onPageClick = {}
            )
        }
    }
}
