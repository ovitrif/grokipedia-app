package io.github.grokipedia.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "grokipedia_prefs")

private lateinit var appContext: Context

fun initDataStore(context: Context) {
    appContext = context.applicationContext
}

actual fun createDataStore(): DataStore<Preferences> {
    return appContext.dataStore
}
