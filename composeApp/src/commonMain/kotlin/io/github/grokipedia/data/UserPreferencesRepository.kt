package io.github.grokipedia.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private val textSizeKey = intPreferencesKey("text_size_percent")
    private val autoFocusKey = booleanPreferencesKey("auto_focus_enabled")
    private val themeKey = stringPreferencesKey("theme")

    val textSizePercent: Flow<Int> = dataStore.data.map { preferences ->
        preferences[textSizeKey] ?: 100
    }

    val autoFocusEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[autoFocusKey] ?: true
    }

    val theme: Flow<String> = dataStore.data.map { preferences ->
        preferences[themeKey] ?: "dark"
    }

    suspend fun setTextSize(percent: Int) {
        dataStore.edit { preferences ->
            preferences[textSizeKey] = percent.coerceIn(80, 160)
        }
    }

    suspend fun setAutoFocus(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[autoFocusKey] = enabled
        }
    }

    suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[themeKey] = theme
        }
    }
}
