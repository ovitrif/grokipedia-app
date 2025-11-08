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
data class SavedPage(
    val url: String,
    val title: String,
    val timestamp: Long = 0L // Will be set when saving
)

class SavedPagesRepository(private val dataStore: DataStore<Preferences>) {
    private val savedPagesKey = stringPreferencesKey("saved_pages")
    private val json = Json { ignoreUnknownKeys = true }

    val savedPages: Flow<List<SavedPage>> = dataStore.data.map { preferences ->
        val jsonString = preferences[savedPagesKey] ?: "[]"
        try {
            json.decodeFromString<List<SavedPage>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun savePage(url: String, title: String) {
        dataStore.edit { preferences ->
            val currentPages = try {
                val jsonString = preferences[savedPagesKey] ?: "[]"
                json.decodeFromString<List<SavedPage>>(jsonString).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            
            // Remove if already exists (to avoid duplicates)
            currentPages.removeAll { it.url == url }
            
            // Add new page at the beginning with current timestamp
            val timestamp = currentPages.maxOfOrNull { it.timestamp }?.plus(1) ?: 1L
            currentPages.add(0, SavedPage(url, title, timestamp))
            
            // Keep only last 100 pages
            if (currentPages.size > 100) {
                currentPages.subList(100, currentPages.size).clear()
            }
            
            preferences[savedPagesKey] = json.encodeToString(currentPages)
        }
    }

    suspend fun removePage(url: String) {
        dataStore.edit { preferences ->
            val currentPages = try {
                val jsonString = preferences[savedPagesKey] ?: "[]"
                json.decodeFromString<List<SavedPage>>(jsonString).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            
            currentPages.removeAll { it.url == url }
            preferences[savedPagesKey] = json.encodeToString(currentPages)
        }
    }

    suspend fun isPageSaved(url: String): Boolean {
        val pages = try {
            dataStore.data.map { preferences ->
                val jsonString = preferences[savedPagesKey] ?: "[]"
                json.decodeFromString<List<SavedPage>>(jsonString)
            }
        } catch (e: Exception) {
            return false
        }
        return false // TODO: implement properly with Flow
    }
}
