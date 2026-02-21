package io.github.grokipedia.screenshot

import io.github.grokipedia.LightColorScheme
import io.github.grokipedia.data.ReadingHistoryRepository
import io.github.grokipedia.data.SavedPagesRepository
import io.github.grokipedia.data.UserPreferencesRepository
import io.github.grokipedia.screens.SettingsScreen
import org.junit.Test

class SettingsScreenScreenshotTest : ScreenshotTestBase() {

    @Test
    fun settingsScreen_default() {
        val dataStore = createTestDataStore()
        captureScreenshot("SettingsScreen_default") {
            SettingsScreen(
                userPreferencesRepository = UserPreferencesRepository(dataStore),
                historyRepository = ReadingHistoryRepository(dataStore),
                savedPagesRepository = SavedPagesRepository(dataStore),
                onNavigateBack = {}
            )
        }
    }

    @Test
    fun settingsScreen_light() {
        val dataStore = createTestDataStore()
        captureScreenshot("SettingsScreen_light", colorScheme = LightColorScheme) {
            SettingsScreen(
                userPreferencesRepository = UserPreferencesRepository(dataStore),
                historyRepository = ReadingHistoryRepository(dataStore),
                savedPagesRepository = SavedPagesRepository(dataStore),
                onNavigateBack = {}
            )
        }
    }
}
