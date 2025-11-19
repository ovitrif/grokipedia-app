# WARP.md

This file provides guidance to ai agents like WARP (warp.dev) and Claude Code when working with code in this repository.

This file is symlinked for cross-agents compatibility to the following paths:
- `CLAUDE.md`
- `WARP.md`

## Project Overview

Grokipedia is a **Compose Multiplatform** mobile application that provides a native wrapper for the Grokipedia website (https://grokipedia.com/). The app targets both **Android** and **iOS** platforms using shared Kotlin code.

### Technology Stack
- **Kotlin**: 2.2.20
- **Compose Multiplatform**: 1.9.3
- **Android Gradle Plugin**: 8.13.0
- **Compose BOM**: 2025.10.01
- **WebView Library**: compose-webview-multiplatform 2.0.3
- **Blur Effects**: haze 1.1.0
- **Data Persistence**: DataStore Preferences 1.1.1

### Build Requirements
- **Java**: OpenJDK 17 or higher
- **Gradle**: 8.14.3 (wrapper included)
- **Android SDK**: compileSdk 36, minSdk 24, targetSdk 36
- **Xcode**: Required for iOS builds (macOS only)

## 🚨 CRITICAL: Test-Driven Development Workflow

**IMPORTANT**: For every new feature or UI change, you MUST follow this workflow:

### 1. Write the UI Test First
Before implementing any feature, create an instrumentation test in `composeApp/src/androidInstrumentedTest/kotlin/io/github/grokipedia/`:

```kotlin
@Test
fun newFeature_behavesAsExpected() {
    // Test the feature behavior
    composeTestRule.onNodeWithTag("feature_element").assertExists()
    composeTestRule.onNodeWithTag("feature_element").performClick()
    // Verify expected outcomes
}
```

### 2. Implement the Feature
Add the feature code with proper `testTag` modifiers for UI elements:

```kotlin
Button(
    onClick = { /* action */ },
    modifier = Modifier.testTag("feature_element")
) {
    Text("Feature Button")
}
```

### 3. Run the Test and Verify
```bash
# Build and install the app
./gradlew :composeApp:assembleDebug
APK_PATH=$(find composeApp -type f -iname "*debug*.apk" | head -n1)
adb install -r "$APK_PATH"

# Run the specific test
./gradlew :composeApp:connectedDebugAndroidTest \
  --tests "io.github.grokipedia.YourNewTest"

# View test results
open composeApp/build/reports/androidTests/connected/debug/index.html
```

### 4. Debug Using ADB Logs
Monitor logs while the test runs to debug issues:

```bash
# In a separate terminal, watch logs in real-time
adb logcat | grep -E "(grokipedia|FOCUS|chromium|WebView|Console|AndroidRuntime)"

# Or capture logs after test run
adb logcat -d > test_logs.txt
```

### 5. Take Screenshots for Verification
Use the test to capture screenshots and verify UI visually:

```kotlin
@Test
fun newFeature_visualVerification() {
    // Trigger the feature
    composeTestRule.onNodeWithTag("feature_element").performClick()
    
    // Take screenshot using UIAutomator
    val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val screenshotFile = File(context.getExternalFilesDir(null), "feature_screenshot.png")
    uiDevice.takeScreenshot(screenshotFile)
}
```

Screenshots are saved to: `/sdcard/Android/data/io.github.grokipedia/files/`

### 6. Verify Implementation Meets Specs
- ✅ Test passes without errors
- ✅ Screenshots show correct UI behavior
- ✅ ADB logs show no errors or warnings
- ✅ Feature works as specified

### Why This Workflow Is Critical
- **Catches issues early**: Tests fail before the feature is considered complete
- **Documents behavior**: Tests serve as executable documentation
- **Enables debugging**: ADB logs help identify root causes quickly
- **Visual verification**: Screenshots confirm UI matches specifications
- **Regression prevention**: Tests ensure features don't break in future

## Common Commands

### Building

```bash
# Build Android debug APK
./gradlew :composeApp:assembleDebug

# Build Android release APK
./gradlew :composeApp:assembleRelease

# Clean build
./gradlew clean

# Build with info logging
./gradlew :composeApp:assembleDebug --info
```

Output: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`

### Running on Android

```bash
# Install on connected device/emulator
export ANDROID_HOME=~/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools

APK_PATH=$(find composeApp -type f -iname "*debug*.apk" | head -n1)
adb install -r "$APK_PATH"

# Launch app
adb shell monkey -p io.github.grokipedia -c android.intent.category.LAUNCHER 1

# View logs
adb logcat | grep -i grokipedia
```

### Testing

#### Android Instrumentation Tests

```bash
# Run all instrumentation tests (ensure only emulator is connected)
./gradlew :composeApp:connectedDebugAndroidTest

# Run specific test class
./gradlew :composeApp:connectedDebugAndroidTest \
  --tests "io.github.grokipedia.AppSmokeTest"

# Run specific test method
./gradlew :composeApp:connectedDebugAndroidTest \
  --tests "io.github.grokipedia.AppSmokeTest.appLaunches_andWebViewIsDisplayed"

# Clean and rerun tests
./gradlew :composeApp:cleanConnectedDebugAndroidTest :composeApp:connectedDebugAndroidTest

# View test results
open composeApp/build/reports/androidTests/connected/debug/index.html
```

**Important**: Disconnect physical devices before running tests to avoid running on multiple devices simultaneously. Use `adb devices` to verify only the emulator is connected.

#### Maestro E2E Tests

```bash
# Install Maestro (first time only)
brew tap mobile-dev-inc/tap
brew install maestro

# Run all Maestro tests
maestro test .maestro

# Run specific flow
maestro test .maestro/01_smoke.yaml
maestro test .maestro/02_navigation.yaml
maestro test .maestro/03_visual_regression.yaml

# Run with specific APK
maestro test .maestro --app-file composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

### iOS Development

```bash
# Open iOS project in Xcode
open iosApp/iosApp.xcodeproj

# Build from command line requires Xcode tools
# Best practice: Use Xcode IDE for iOS builds
```

### Gradle Management

```bash
# List all tasks
./gradlew tasks

# Check dependency tree
./gradlew :composeApp:dependencies

# Update Gradle wrapper
./gradlew wrapper --gradle-version 8.14.3
```

## Architecture Overview

### Project Structure

```
grokipedia-app/
├── composeApp/              # Main multiplatform module
│   └── src/
│       ├── commonMain/      # Shared code for all platforms
│       │   └── kotlin/io/github/grokipedia/
│       │       ├── App.kt                    # Main UI and WebViewScreen
│       │       ├── data/
│       │       │   ├── SavedPagesRepository.kt  # Persistent storage
│       │       │   └── DataStoreFactory.kt      # Platform abstraction
│       │       ├── screens/
│       │       │   └── SavedPagesScreen.kt      # Saved pages list UI
│       │       ├── ui/
│       │       │   └── FocusableWebView.kt      # Platform expect/actual
│       │       └── util/
│       │           └── KeyboardManager.kt       # Platform expect/actual
│       ├── androidMain/     # Android-specific implementations
│       │   └── kotlin/io/github/grokipedia/
│       │       ├── MainActivity.kt
│       │       ├── data/DataStoreFactory.android.kt
│       │       ├── ui/FocusableWebView.android.kt
│       │       └── util/KeyboardManager.android.kt
│       ├── androidInstrumentedTest/     # Android instrumentation tests
│       │   └── kotlin/io/github/grokipedia/
│       │       ├── AppSmokeTest.kt
│       │       ├── LoadingIndicatorTest.kt
│       │       └── LogAnalysisTest.kt
│       └── iosMain/         # iOS-specific implementations
│           └── kotlin/io/github/grokipedia/
│               ├── MainViewController.kt
│               ├── data/DataStoreFactory.ios.kt
│               ├── ui/FocusableWebView.ios.kt
│               └── util/KeyboardManager.ios.kt
├── iosApp/                  # iOS application wrapper
├── .maestro/                # E2E tests (cross-platform)
├── docs/                    # Documentation
└── gradle/                  # Build configuration
    └── libs.versions.toml   # Version catalog
```

### Key Components

#### 1. WebView Integration

- **Library**: `compose-webview-multiplatform` 2.0.3
- **Main URL**: https://grokipedia.com/
- **Features**: 
  - Back button navigation with `captureBackPresses`
  - Loading state management
  - Hardware acceleration enabled
  - JavaScript execution support for search auto-focus

#### 2. UI Architecture

**App.kt** contains the main composable with two screens:
- `Screen.WebView`: Main WebView with transparent blurred topbar
- `Screen.SavedPages`: List of saved articles

**Topbar Features**:
- Blur effect using Haze library (`hazeChild` on topbar, `haze` on WebView)
- Conditional back button (hidden on homepage)
- Dropdown menu (3-dot MoreVert icon) with:
  - Home navigation
  - Save/Unsave page (with star icons)
  - Saved pages list access
- Snackbar notifications for user actions

#### 3. Data Persistence

**SavedPagesRepository**:
- Uses DataStore Preferences (cross-platform)
- JSON serialization with kotlinx.serialization
- Stores up to 100 saved pages
- Data model: `SavedPage(url: String, title: String, timestamp: Long)`

**Platform Storage**:
- Android: `/data/data/io.github.grokipedia/files/datastore/grokipedia_prefs.preferences_pb`
- iOS: `NSDocumentDirectory/grokipedia_prefs.preferences_pb`

**Initialization**:
- Android: `initDataStore(context)` called in `MainActivity.onCreate()`
- iOS: Created in DataStoreFactory with platform-specific paths

#### 4. Platform Abstraction Pattern

The app uses Kotlin Multiplatform's `expect`/`actual` pattern for platform-specific functionality:

```kotlin
// commonMain - Declaration
expect fun FocusableWebView(...)
expect class KeyboardManager() { fun showKeyboard() }
expect fun createDataStore(): DataStore<Preferences>

// androidMain - Android implementation
actual fun FocusableWebView(...) { /* Android WebView */ }

// iosMain - iOS implementation  
actual fun FocusableWebView(...) { /* iOS WKWebView */ }
```

#### 5. Auto-Focus Search Feature

**Behavior**: When the homepage loads, the app automatically:
1. Waits 2 seconds for page render
2. Executes JavaScript to find and focus search input
3. Dispatches touch/mouse events
4. Shows mobile keyboard via platform KeyboardManager
5. Only runs once per homepage visit

**Implementation**: Complex JavaScript injection in `App.kt` WebViewScreen with console logging for debugging.

## Important Implementation Details

### WebView Configuration

- **Debugging enabled** in MainActivity: `WebView.setWebContentsDebuggingEnabled(true)`
- **Android Manifest Permissions**:
  - `INTERNET` (required)
  - `ACCESS_NETWORK_STATE` (optional)
  - `usesCleartextTraffic="true"` (HTTP fallback)

### Material 3 Design

- Custom dark color scheme defined in `App.kt`
- Edge-to-edge display with `enableEdgeToEdge()`
- Material icons extended library for menu icons
- Circular progress indicator during page loads

### Back Button Handling

Three layers:
1. Compose `BackHandler` checks `navigator.canGoBack`
2. WebView `captureBackPresses = true`
3. System back button integration

### Gradle Version Catalog

All dependencies managed in `gradle/libs.versions.toml`. To update versions, edit this file rather than build.gradle.kts files directly.

## Testing Strategy

### Unit Tests
Currently no unit tests. Good candidates:
- SavedPagesRepository logic
- URL validation
- JSON serialization/deserialization

### Instrumentation Tests (androidInstrumentedTest/)
- **AppSmokeTest**: Basic launch and WebView visibility
- **LoadingIndicatorTest**: Progress indicator during page load
- **LogAnalysisTest**: Verifies no critical errors in logs
- **KeyboardFocusTest**: Validates search auto-focus

### E2E Tests (.maestro/)
- **01_smoke.yaml**: App launch and basic functionality (~5-10s)
- **02_navigation.yaml**: Navigation flow and back button (~10-15s)
- **03_visual_regression.yaml**: Screenshot capture for visual testing (~15-20s)
- **test_search_article.yaml**: Search interaction flow

## Common Development Tasks

### Adding a New Feature (Follow TDD Workflow Above!)

1. **Write the test first** in `composeApp/src/androidInstrumentedTest/kotlin/io/github/grokipedia/`
2. **Implement the feature** with proper `testTag` modifiers
3. **Run the test** and verify it passes
4. **Monitor ADB logs** for any issues
5. **Capture screenshots** for visual verification
6. **Verify specs are met** before considering the feature complete

### Adding a New Dependency

1. Add version to `gradle/libs.versions.toml`:
   ```toml
   [versions]
   mylib = "1.0.0"
   
   [libraries]
   mylib = { module = "com.example:mylib", version.ref = "mylib" }
   ```

2. Add to appropriate source set in `composeApp/build.gradle.kts`:
   ```kotlin
   commonMain.dependencies {
       implementation(libs.mylib)
   }
   ```

### Adding Platform-Specific Code

1. Declare in `commonMain`:
   ```kotlin
   expect fun platformFunction(): String
   ```

2. Implement in `androidMain`:
   ```kotlin
   actual fun platformFunction(): String = "Android"
   ```

3. Implement in `iosMain`:
   ```kotlin
   actual fun platformFunction(): String = "iOS"
   ```

### Debugging WebView Issues

1. **Android**: Use Chrome DevTools
   - Open `chrome://inspect` in Chrome
   - Select the WebView instance
   - Inspect elements, console logs, network

2. **Check logs**:
   ```bash
   adb logcat | grep -E "(chromium|WebView|Console)"
   ```

3. **JavaScript console**: The auto-focus feature logs extensively with `[FOCUS-JS]` prefix

### Modifying the Topbar

The topbar implementation in `App.kt` uses:
- `Row` with `windowInsetsPadding(WindowInsets.statusBars)` for safe area
- `hazeChild` modifier for blur effect over content
- Fixed height: 56.dp
- WebView has matching `padding(top = 56.dp)`

## Troubleshooting

### Build Failures

**"AAR metadata check failed"**:
- Update AGP version in `gradle/libs.versions.toml`
- Current: `agp = "8.13.0"`

**"Compose Multiplatform version not found"**:
- Verify version exists: https://github.com/JetBrains/compose-multiplatform/releases
- Current: `composeMultiplatform = "1.9.3"`

### Runtime Issues

**WebView not loading**:
- Check internet connectivity
- Verify INTERNET permission in AndroidManifest.xml
- Check URL: should be `https://grokipedia.com/`

**DataStore crashes**:
- Ensure `initDataStore()` called before `App()` composable
- Check file permissions (Android) or document directory access (iOS)

**Keyboard not showing**:
- Verify `initKeyboardManager()` called in MainActivity
- Check JavaScript focus script execution in logs
- Test on physical device (emulator keyboard behavior may differ)

### Test Failures

**"No connected devices"**:
- Start emulator: `emulator -avd Pixel_8_35 -no-snapshot &`
- Wait for boot: `adb wait-for-device`
- Verify ready: `adb shell getprop sys.boot_completed` (returns "1")

**Tests run on wrong device**:
- Check connected: `adb devices`
- Disconnect physical device or use: `ANDROID_SERIAL=emulator-5554 ./gradlew ...`

**Maestro "Element not found"**:
- Increase timeout in YAML file
- Check internet connectivity on device
- Verify content loaded: take screenshot in test flow

## Resources

- **README.md**: Project overview and basic setup
- **docs/BUILD_NOTES.md**: Detailed build configuration history
- **docs/RUN_NOTES.md**: Runtime validation and device setup
- **docs/TESTING_GUIDE.md**: Comprehensive testing instructions
- **docs/feat/MENU_FEATURES.md**: Dropdown menu implementation details
- **.maestro/README.md**: Maestro E2E testing guide
