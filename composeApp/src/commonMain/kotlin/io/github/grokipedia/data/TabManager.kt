package io.github.grokipedia.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class BrowserTab(
    val id: String,
    val url: String,
    val title: String,
    val timestamp: Long = 0L
)

@Serializable
data class TabState(
    val tabs: List<BrowserTab> = listOf(BrowserTab("tab_0", "https://grokipedia.com/", "Grokipedia", 0L)),
    val activeTabId: String = "tab_0"
)

class TabManager(private val dataStore: DataStore<Preferences>) {
    private val tabStateKey = stringPreferencesKey("tab_state")
    private val json = Json { ignoreUnknownKeys = true }

    val tabState: Flow<TabState> = dataStore.data.map { preferences ->
        val jsonString = preferences[tabStateKey]
        if (jsonString != null) {
            try {
                json.decodeFromString<TabState>(jsonString)
            } catch (e: Exception) {
                TabState()
            }
        } else {
            TabState()
        }
    }

    suspend fun createTab(url: String = "https://grokipedia.com/", title: String = "New Tab"): String {
        var newTabId = ""
        dataStore.edit { preferences ->
            val state = getCurrentState(preferences)
            val id = "tab_${System.currentTimeMillis()}"
            val timestamp = state.tabs.maxOfOrNull { it.timestamp }?.plus(1) ?: 1L
            val newTab = BrowserTab(id, url, title, timestamp)
            val updatedTabs = (state.tabs + newTab).takeLast(10)
            val newState = state.copy(tabs = updatedTabs, activeTabId = id)
            preferences[tabStateKey] = json.encodeToString(newState)
            newTabId = id
            println("[TABS] Created: $id, total: ${updatedTabs.size}")
        }
        return newTabId
    }

    suspend fun closeTab(tabId: String) {
        dataStore.edit { preferences ->
            val state = getCurrentState(preferences)
            val updatedTabs = state.tabs.filter { it.id != tabId }
            if (updatedTabs.isEmpty()) {
                // Always keep at least one tab
                val defaultTab = BrowserTab("tab_${System.currentTimeMillis()}", "https://grokipedia.com/", "Grokipedia", 0L)
                val newState = TabState(tabs = listOf(defaultTab), activeTabId = defaultTab.id)
                preferences[tabStateKey] = json.encodeToString(newState)
            } else {
                val activeId = if (state.activeTabId == tabId) updatedTabs.last().id else state.activeTabId
                val newState = state.copy(tabs = updatedTabs, activeTabId = activeId)
                preferences[tabStateKey] = json.encodeToString(newState)
            }
            println("[TABS] Closed: $tabId")
        }
    }

    suspend fun switchTab(tabId: String) {
        dataStore.edit { preferences ->
            val state = getCurrentState(preferences)
            val newState = state.copy(activeTabId = tabId)
            preferences[tabStateKey] = json.encodeToString(newState)
            println("[TABS] Switched to: $tabId")
        }
    }

    suspend fun updateTabInfo(tabId: String, url: String, title: String) {
        dataStore.edit { preferences ->
            val state = getCurrentState(preferences)
            val updatedTabs = state.tabs.map {
                if (it.id == tabId) it.copy(url = url, title = title) else it
            }
            val newState = state.copy(tabs = updatedTabs)
            preferences[tabStateKey] = json.encodeToString(newState)
        }
    }

    suspend fun closeAllTabs() {
        dataStore.edit { preferences ->
            val defaultTab = BrowserTab("tab_${System.currentTimeMillis()}", "https://grokipedia.com/", "Grokipedia", 0L)
            val newState = TabState(tabs = listOf(defaultTab), activeTabId = defaultTab.id)
            preferences[tabStateKey] = json.encodeToString(newState)
            println("[TABS] Closed all tabs")
        }
    }

    private fun getCurrentState(preferences: Preferences): TabState {
        val jsonString = preferences[tabStateKey]
        return if (jsonString != null) {
            try {
                json.decodeFromString<TabState>(jsonString)
            } catch (e: Exception) {
                TabState()
            }
        } else {
            TabState()
        }
    }
}
