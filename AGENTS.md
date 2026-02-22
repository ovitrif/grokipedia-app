# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

This file is symlinked for cross-agents compatibility:
- `CLAUDE.md` -> `AGENTS.md`
- `WARP.md` -> `AGENTS.md`

## Project Overview

Grokipedia is a **Compose Multiplatform** mobile application wrapping https://grokipedia.com/ for Android and iOS using shared Kotlin code.

**Key Libraries**: compose-webview-multiplatform (WebView), haze (blur effects), DataStore Preferences (persistence)

## AI Autonomy Principle

This project is an experiment in autonomous AI development — the goal is a working, production-ready, store-released multiplatform app entirely developed and validated by AI.

**Core rules:**

1. **AI is fully responsible for self-verification.** Code is NOT done until tests pass and screenshots prove it works.
2. **Compilation alone is never sufficient.** Every change must be verified on-device with Maestro tests, screenshots, and log analysis.
3. **The verification loop is mandatory, not optional:** implement → test → read screenshots/logs → fix → repeat until clean.
4. **No feature ships without evidence.** Screenshots must visually confirm correct behavior. Error logs must be clean.

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

# Screenshot tests (Roborazzi) — no emulator needed
./scripts/screenshot-test.sh record    # record new references
./scripts/screenshot-test.sh verify    # verify against references
./scripts/screenshot-test.sh compare   # generate diffs
./gradlew :composeApp:testDebugUnitTest --tests "...ScreenshotTest"

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

**Commit message suggestions**: After completing implementation work, ALWAYS suggest exactly 3 commit messages under 50 characters using the conventional commits format (e.g., `feat:`, `fix:`, `refactor:`). Present them as a numbered list for the user to pick from.

## Architecture

```
composeApp/src/
├── commonMain/kotlin/io/github/grokipedia/
│   ├── App.kt              # Main UI with WebViewScreen, navigation, and overflow sheet
│   ├── data/
│   │   ├── SavedPagesRepository.kt   # Favorites persistence (DataStore, max 100)
│   │   ├── ReadingHistoryRepository.kt # Browsing history (DataStore, max 200)
│   │   ├── TabManager.kt             # Tab state management (DataStore, max 10)
│   │   ├── UserPreferencesRepository.kt # Settings: text size, theme, auto-focus
│   │   └── DataStoreFactory.kt       # expect/actual DataStore creation
│   ├── screens/
│   │   ├── SavedPagesScreen.kt       # Saved articles list
│   │   ├── HistoryScreen.kt          # Browsing history with clear
│   │   ├── SettingsScreen.kt         # Preferences: reading, appearance, data, about
│   │   └── TabSwitcherScreen.kt      # Tab grid with create/close/switch
│   ├── ui/
│   │   ├── FocusableWebView.kt       # expect/actual WebView wrapper
│   │   ├── FindInPageBar.kt          # In-page search with match count + nav
│   │   ├── TableOfContentsSheet.kt   # TOC bottom sheet from page headings
│   │   └── TextSizeControls.kt       # A-/A+ size controls (80-160%)
│   └── util/
│       ├── KeyboardManager.kt        # expect/actual keyboard show/hide
│       └── ShareManager.kt           # expect/actual platform sharing
├── androidMain/            # actual implementations for Android
├── androidInstrumentedTest/ # UI tests (instrumented, requires emulator)
├── test/                   # JVM screenshot tests (Roborazzi, no emulator)
│   ├── kotlin/io/github/grokipedia/screenshot/
│   │   ├── ScreenshotTestBase.kt       # Abstract base with captureScreenshot()
│   │   ├── TestDataStoreFactory.kt     # In-memory DataStore for tests
│   │   └── *ScreenshotTest.kt          # Screenshot tests per composable
│   ├── resources/robolectric.properties # Robolectric config (SDK 34)
│   └── screenshots/                    # Git-tracked reference PNGs
└── iosMain/                # actual implementations for iOS
```

### Platform Abstraction (expect/actual)

Platform-specific code uses Kotlin Multiplatform's `expect`/`actual` pattern:
- `FocusableWebView` - WebView wrapper with keyboard focus
- `KeyboardManager` - Show/hide keyboard
- `DataStoreFactory` - Persistent storage initialization
- `ShareManager` - Platform share sheet (Intent on Android, UIActivityViewController on iOS)

### Key Components

**App.kt** - Main entry point with:
- Five screens: `WebView` (main), `SavedPages`, `History`, `Settings`, `TabSwitcher`
- Floating navigation buttons (bottom-right): More, Back, Home, Save/Unsave, Saved Pages
- Overflow bottom sheet (More button): Share, Text Size, Find in Page, TOC, History, Tabs, Settings
- CSS injection for theme colors, text size, and topbar offset
- Auto-focus search on homepage (JavaScript injection after 2s delay)
- 4 theme color schemes: Dark (default), Light, Sepia, Black

**Data Layer** - All DataStore-backed with JSON serialization:
- `SavedPagesRepository` - Favorites (max 100 entries)
- `ReadingHistoryRepository` - Auto-tracked browsing history (max 200)
- `TabManager` - Multi-tab state (max 10 tabs)
- `UserPreferencesRepository` - Text size, theme, auto-focus toggle

**Initialization order**:
- Android: `MainActivity.onCreate()` calls `initDataStore(context)`, `initKeyboardManager()`, `initShareManager()` before `App()`
- iOS: DataStoreFactory handles paths via `NSDocumentDirectory`

## Screenshot Testing (Roborazzi)

JVM-based screenshot tests using Roborazzi + Robolectric. No emulator needed — runs on JVM with native graphics.

### Commands

```bash
./scripts/screenshot-test.sh record    # record/update reference screenshots
./scripts/screenshot-test.sh verify    # verify against references (CI)
./scripts/screenshot-test.sh compare   # generate diff images

# Run a specific screenshot test
./gradlew :composeApp:testDebugUnitTest --tests "io.github.grokipedia.screenshot.TextSizeControlsScreenshotTest"
```

### Rules

1. **Every visual change requires screenshot test update** — re-record references after UI modifications
2. **New composables in `ui/` or `screens/` need a `*ScreenshotTest.kt`** in `src/test/kotlin/.../screenshot/`
3. **Always run `./scripts/screenshot-test.sh verify` before considering visual work done**
4. **Reference images are version-controlled** — commit updated PNGs after re-recording
5. **Test composables in isolation** via `composeTestRule.setContent { }`, not Activity launch
6. **For `ModalBottomSheet` composables**, extract inner content and test that directly
7. **Test multiple themes** — at minimum dark + light variants
8. **Every composable MUST have a `@Preview` function** — add a `@Composable @Preview private fun <Name>Preview()` for each public composable in `ui/` and `screens/`. Use `org.jetbrains.compose.ui.tooling.preview.Preview` for KMP compatibility. Provide representative sample data so the preview is useful in IDE tooling.

### Writing a Screenshot Test

```kotlin
class MyComponentScreenshotTest : ScreenshotTestBase() {
    @Test
    fun myComponent_default() {
        captureScreenshot("MyComponent_default") {
            MyComponent(param1 = "value", onAction = {})
        }
    }

    @Test
    fun myComponent_light() {
        captureScreenshot("MyComponent_light", colorScheme = LightColorScheme) {
            MyComponent(param1 = "value", onAction = {})
        }
    }
}
```

For screens that need DataStore repositories, use `createTestDataStore()` from `TestDataStoreFactory.kt`.

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
