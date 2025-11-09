# Dropdown Menu & Saved Pages Implementation

## Overview
Successfully implemented a dropdown action menu in the topbar with "Home" and "Save Page" features, using DataStore Preferences for cross-platform persistence.

## Features Implemented

### 1. Dropdown Menu Icon
- **Location**: Top-right corner of the topbar
- **Icon**: Three vertical dots (MoreVert)
- **Behavior**: Opens dropdown menu on tap

### 2. Home Feature
- **Menu Item**: "Home" with home icon
- **Functionality**: Navigates back to https://grokipedia.com/
- **Implementation**: Uses `navigator.loadUrl()` to load the home page

### 3. Save/Unsave Page Feature
- **Menu Item**: Dynamically shows "Save Page" or "Unsave Page"
- **Icon**: Outlined star (not saved) or filled star (saved)
- **Functionality**:
  - Saves current page URL and title to DataStore
  - Removes page from saved list if already saved
  - Shows snackbar notification ("Page saved" or "Page removed")
  - Updates icon state immediately

### 4. Snackbar Notifications
- **Location**: Bottom center of screen
- **Messages**:
  - "Page saved" when a page is saved
  - "Page removed" when a page is unsaved

## Technical Implementation

### DataStore Preferences (Multiplatform)
- **Library**: androidx.datastore:datastore-preferences-core:1.1.1
- **Storage Format**: JSON-serialized list of SavedPage objects
- **Platform Support**: Android and iOS

### Data Model
```kotlin
@Serializable
data class SavedPage(
    val url: String,
    val title: String,
    val timestamp: Long
)
```

### Repository Pattern
```kotlin
class SavedPagesRepository(private val dataStore: DataStore<Preferences>)
- savedPages: Flow<List<SavedPage>>
- savePage(url: String, title: String)
- removePage(url: String)
```

### Platform-Specific Implementations

**Android** (`DataStoreFactory.android.kt`):
- Uses `preferencesDataStore` delegate
- Storage: `/data/data/io.github.grokipedia/files/datastore/grokipedia_prefs.preferences_pb`
- Initialized in `MainActivity.onCreate()`

**iOS** (`DataStoreFactory.ios.kt`):
- Uses `PreferenceDataStoreFactory.createWithPath()`
- Storage: `NSDocumentDirectory/grokipedia_prefs.preferences_pb`
- Path conversion using `okio.Path`

## Files Created/Modified

### New Files:
1. `composeApp/src/commonMain/kotlin/io/github/grokipedia/data/SavedPagesRepository.kt`
2. `composeApp/src/commonMain/kotlin/io/github/grokipedia/data/DataStoreFactory.kt`
3. `composeApp/src/androidMain/kotlin/io/github/grokipedia/data/DataStoreFactory.android.kt`
4. `composeApp/src/iosMain/kotlin/io/github/grokipedia/data/DataStoreFactory.ios.kt`

### Modified Files:
1. `composeApp/src/commonMain/kotlin/io/github/grokipedia/App.kt` - Added dropdown menu UI
2. `composeApp/src/androidMain/kotlin/io/github/grokipedia/MainActivity.kt` - Initialize DataStore
3. `composeApp/build.gradle.kts` - Added dependencies
4. `gradle/libs.versions.toml` - Added version catalog entries

## Dependencies Added
- `androidx.datastore:datastore-preferences-core:1.1.1` (common)
- `androidx.datastore:datastore-preferences:1.1.1` (Android)
- `org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3` (common)
- `kotlin("plugin.serialization")` plugin

## Storage Limits
- Maximum 100 saved pages
- Older pages are automatically removed when limit is exceeded
- Duplicates are prevented (same URL can't be saved twice)

## UI/UX Details
- **Topbar Layout**: `[Back Button] [Spacer] [Menu Button]`
- **Menu Position**: Anchored below the menu button
- **Icon States**: Gray when disabled (back), white when enabled
- **Star Icon**: Filled when page is saved, outlined when not saved
- **Blur Effect**: Menu appears over the blurred topbar

## Testing
The implementation works on both platforms:
- ✅ Android build successful
- ✅ iOS build successful
- ✅ Cross-platform persistence via DataStore
- ✅ Proper error handling for JSON serialization

## Usage Flow
1. User taps three-dot menu icon
2. Menu opens with "Home" and "Save Page" options
3. User taps "Save Page" to save current article
4. Snackbar confirms "Page saved"
5. Icon changes to filled star
6. Menu item text changes to "Unsave Page"
7. Saved pages persist across app restarts

## Future Enhancements (Not Implemented)
- View list of saved pages
- Search through saved pages
- Export/import saved pages
- Sync saved pages across devices
- Folders/categories for saved pages
