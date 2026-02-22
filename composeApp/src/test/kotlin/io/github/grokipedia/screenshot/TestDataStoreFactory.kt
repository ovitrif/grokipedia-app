package io.github.grokipedia.screenshot

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toOkioPath
import java.io.File

fun createTestDataStore(): DataStore<Preferences> {
    val file = File.createTempFile("test_datastore_", ".preferences_pb")
    file.deleteOnExit()
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { file.toOkioPath() }
    )
}
