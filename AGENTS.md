# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

This file is symlinked for cross-agents compatibility:
- `CLAUDE.md` -> `AGENTS.md`
- `WARP.md` -> `AGENTS.md`

## Project Overview

Grokipedia is a **Compose Multiplatform** mobile application wrapping https://grokipedia.com/ for Android and iOS using shared Kotlin code.

**Key Libraries**: compose-webview-multiplatform (WebView), haze (blur effects), DataStore Preferences (persistence)

## Build Requirements

- Java: OpenJDK 17+
- Android SDK: compileSdk 36, minSdk 24
- Xcode: Required for iOS (macOS only)
- Versions managed in `gradle/libs.versions.toml`

## Common Commands

```bash
# Build
./gradlew :composeApp:assembleDebug

# Install and run on Android
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
adb shell monkey -p io.github.grokipedia -c android.intent.category.LAUNCHER 1

# Run all Android instrumentation tests
./gradlew :composeApp:connectedDebugAndroidTest

# Run specific test class
./gradlew :composeApp:connectedDebugAndroidTest --tests "io.github.grokipedia.AppSmokeTest"

# Run specific test method
./gradlew :composeApp:connectedDebugAndroidTest --tests "io.github.grokipedia.AppSmokeTest.appLaunches_andWebViewIsDisplayed"

# View test results
open composeApp/build/reports/androidTests/connected/debug/index.html

# Maestro E2E tests
maestro test .maestro/04_core_functionalities.yaml

# Debug logs
adb logcat | grep -E "(grokipedia|FOCUS|chromium|WebView|Console|AndroidRuntime)"

# iOS - open in Xcode
open iosApp/iosApp.xcodeproj
```

## Test-Driven Development Workflow

**IMPORTANT**: For every new feature or UI change, follow this workflow:

1. **Write test first** in `composeApp/src/androidInstrumentedTest/kotlin/io/github/grokipedia/`
2. **Add testTag modifiers** to UI elements: `Modifier.testTag("element_name")`
3. **Run test**: `./gradlew :composeApp:connectedDebugAndroidTest --tests "io.github.grokipedia.YourTest"`
4. **Monitor logs**: `adb logcat | grep -E "(grokipedia|FOCUS|chromium|WebView)"`

**Important**: Disconnect physical devices before running instrumentation tests. Use `adb devices` to verify only emulator is connected.

## Self-Verification Workflow (Claude Code)

Claude Code can autonomously verify implementations using visual and log analysis.

### Verification Commands

```bash
# Full verification with video recording and frame extraction
./scripts/run-verification.sh .maestro/04_core_functionalities.yaml

# Quick verification (screenshots + logs only, faster)
./scripts/quick-verify.sh .maestro/04_core_functionalities.yaml

# Analyze results from last run
./scripts/analyze-logs.sh
```

### Verification Artifacts

After running verification, artifacts are saved to `.verification/latest/`:
- `screenshots/` - Screenshots at each test step (view with Read tool)
- `frames/` - Video frames extracted at 1fps (full verification only)
- `logs/app.txt` - All logs from app process (crashes, exceptions, prints)
- `logs/tagged.txt` - Only `[FOCUS]`, `[KEYBOARD]`, `[TEST]` tagged logs
- `logs/errors.txt` - Errors/exceptions/crashes extracted from app logs
- `logs/webview_errors.txt` - WebView/chromium specific errors
- `logs/logcat.txt` - Full unfiltered logcat
- `SUMMARY.md` - Run summary with pass/fail status (full verification only)

### Iteration Loop

1. **Implement feature** with proper `testTag` modifiers
2. **Create/update Maestro test** in `.maestro/` (copy from `00_verification_template.yaml`)
3. **Run verification**: `./scripts/quick-verify.sh .maestro/your_test.yaml`
4. **Analyze results**:
   - Read screenshots: `Read .verification/latest/screenshots/01_*.png`
   - Check errors: `Read .verification/latest/logs/errors.txt`
   - Review summary: `Read .verification/latest/SUMMARY.md`
5. **Fix issues and repeat** until verification passes

### Creating Feature Tests

Copy the template and add assertions:
```yaml
# .maestro/feature_name_test.yaml
appId: io.github.grokipedia
---
- launchApp
- takeScreenshot: 01_initial
- tapOn:
    text: "Your Element"
- takeScreenshot: 02_after_tap
- assertVisible:
    text: "Expected Result"
- takeScreenshot: 03_verified
```

### Log Tags for Debugging

Add logs in Kotlin code using these prefixes (filtered automatically):
- `[FOCUS]` - Auto-focus feature
- `[KEYBOARD]` - Keyboard operations
- `[TEST]` - Test-related logs
- Use `println("[TAG] message")` in Kotlin code

## Output Rules

**Pixel perfection for UI alignment**: When implementing UI changes, always verify visual alignment against reference screenshots. Use exact measurements and compare against existing UI elements (e.g., align floating buttons with existing icons on the page). If a screenshot shows misalignment, fix it before considering the task complete.

**Screenshot paths in final messages**: When implementing features with visual changes, include clickable paths to relevant screenshots in your final summary message:
```
Screenshots:
- .verification/latest/screenshots/01_initial.png
- .verification/latest/screenshots/02_after_change.png
```
This enables direct clicking in Warp terminal for quick visual review.

## Architecture

```
composeApp/src/
├── commonMain/kotlin/io/github/grokipedia/
│   ├── App.kt              # Main UI with WebViewScreen and navigation
│   ├── data/               # SavedPagesRepository, DataStoreFactory (expect)
│   ├── screens/            # SavedPagesScreen
│   ├── ui/                 # FocusableWebView (expect)
│   └── util/               # KeyboardManager (expect)
├── androidMain/            # actual implementations for Android
├── androidInstrumentedTest/ # UI tests
└── iosMain/                # actual implementations for iOS
```

### Platform Abstraction (expect/actual)

Platform-specific code uses Kotlin Multiplatform's `expect`/`actual` pattern:
- `FocusableWebView` - WebView wrapper with keyboard focus
- `KeyboardManager` - Show/hide keyboard
- `DataStoreFactory` - Persistent storage initialization

### Key Components

**App.kt** - Main entry point with:
- Two screens: `Screen.WebView` (main), `Screen.SavedPages` (saved articles)
- Blurred topbar using Haze library (`hazeChild` on topbar, `haze` on content)
- Dropdown menu: Home, Save/Unsave page, Saved pages list
- Auto-focus search on homepage (JavaScript injection after 2s delay)

**SavedPagesRepository** - DataStore-backed storage with JSON serialization, stores up to 100 `SavedPage(url, title, timestamp)` entries

**Initialization order**:
- Android: `MainActivity.onCreate()` calls `initDataStore(context)` and `initKeyboardManager()` before `App()`
- iOS: DataStoreFactory handles paths via `NSDocumentDirectory`

## Debugging WebView

1. Chrome DevTools: `chrome://inspect` to inspect WebView
2. JavaScript logging uses `[FOCUS-JS]` prefix
3. WebView debugging enabled in MainActivity: `WebView.setWebContentsDebuggingEnabled(true)`

## Adding Dependencies

1. Add to `gradle/libs.versions.toml`:
   ```toml
   [versions]
   mylib = "1.0.0"
   [libraries]
   mylib = { module = "com.example:mylib", version.ref = "mylib" }
   ```
2. Add to source set in `composeApp/build.gradle.kts`:
   ```kotlin
   commonMain.dependencies { implementation(libs.mylib) }
   ```

## Troubleshooting

**Tests fail with "No connected devices"**: Start emulator and wait for boot:
```bash
adb wait-for-device && adb shell getprop sys.boot_completed
```

**DataStore crashes**: Ensure `initDataStore()` called before `App()` composable

**Keyboard not showing**: Check `initKeyboardManager()` called and JavaScript focus logs
