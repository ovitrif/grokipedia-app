package io.github.grokipedia.screenshot

import io.github.grokipedia.LightColorScheme
import io.github.grokipedia.data.SavedPagesRepository
import io.github.grokipedia.screens.SavedPagesScreen
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SavedPagesScreenScreenshotTest : ScreenshotTestBase() {

    @Test
    fun savedPagesScreen_empty() {
        val dataStore = createTestDataStore()
        captureScreenshot("SavedPagesScreen_empty") {
            SavedPagesScreen(
                repository = SavedPagesRepository(dataStore),
                onNavigateBack = {},
                onPageClick = {}
            )
        }
    }

    @Test
    fun savedPagesScreen_withItems() {
        val dataStore = createTestDataStore()
        val repository = SavedPagesRepository(dataStore)
        runBlocking {
            repository.savePage("https://grokipedia.com/wiki/Kotlin", "Kotlin (programming language)")
            repository.savePage("https://grokipedia.com/wiki/Compose", "Jetpack Compose")
            repository.savePage("https://grokipedia.com/wiki/Android", "Android (operating system)")
        }

        captureScreenshot("SavedPagesScreen_withItems") {
            SavedPagesScreen(
                repository = repository,
                onNavigateBack = {},
                onPageClick = {}
            )
        }
    }

    @Test
    fun savedPagesScreen_empty_light() {
        val dataStore = createTestDataStore()
        captureScreenshot("SavedPagesScreen_empty_light", colorScheme = LightColorScheme) {
            SavedPagesScreen(
                repository = SavedPagesRepository(dataStore),
                onNavigateBack = {},
                onPageClick = {}
            )
        }
    }
}
