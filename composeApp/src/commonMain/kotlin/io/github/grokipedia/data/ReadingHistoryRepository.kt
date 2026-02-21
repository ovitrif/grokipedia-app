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
data class HistoryItem(
    val url: String,
    val title: String,
    val timestamp: Long = 0L
)

class ReadingHistoryRepository(private val dataStore: DataStore<Preferences>) {
    private val historyKey = stringPreferencesKey("reading_history")
    private val json = Json { ignoreUnknownKeys = true }

    val history: Flow<List<HistoryItem>> = dataStore.data.map { preferences ->
        val jsonString = preferences[historyKey] ?: "[]"
        try {
            json.decodeFromString<List<HistoryItem>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addToHistory(url: String, title: String) {
        dataStore.edit { preferences ->
            val currentHistory = try {
                val jsonString = preferences[historyKey] ?: "[]"
                json.decodeFromString<List<HistoryItem>>(jsonString).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }

            // Remove if already exists (will re-add at top)
            currentHistory.removeAll { it.url == url }

            // Add at beginning with current timestamp
            val timestamp = currentHistory.maxOfOrNull { it.timestamp }?.plus(1) ?: 1L
            currentHistory.add(0, HistoryItem(url, title, timestamp))

            // Keep only last 200 items
            if (currentHistory.size > 200) {
                currentHistory.subList(200, currentHistory.size).clear()
            }

            preferences[historyKey] = json.encodeToString(currentHistory)
            println("[HISTORY] Added: $url")
        }
    }

    suspend fun removeItem(url: String) {
        dataStore.edit { preferences ->
            val currentHistory = try {
                val jsonString = preferences[historyKey] ?: "[]"
                json.decodeFromString<List<HistoryItem>>(jsonString).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }

            currentHistory.removeAll { it.url == url }
            preferences[historyKey] = json.encodeToString(currentHistory)
        }
    }

    suspend fun clearHistory() {
        dataStore.edit { preferences ->
            preferences[historyKey] = "[]"
            println("[HISTORY] Cleared")
        }
    }
}
