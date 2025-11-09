# Testing Strategy - Grokipedia App

## Overview

This document outlines the automated testing strategy for the Grokipedia Compose Multiplatform app, covering Android and iOS platforms with focus on UI interactions, WebView verification, screenshot comparison, and log analysis.

## Testing Goals

1. **UI Verification**: Validate that UI components render correctly without manual inspection
2. **WebView Validation**: Verify WebView loads content and can interact with web pages
3. **Log Analysis**: Automatically capture and analyze logs for errors and warnings
4. **Cross-platform**: Support both Android and iOS platforms
5. **Screenshot Testing**: Visual regression testing for UI consistency
6. **CI/CD Integration**: All tests runnable in automated pipelines

## Recommended Multi-layered Testing Approach

### Layer 1: Android Compose UI Tests (Fast Feedback)
**Tool**: Jetpack Compose Testing + Espresso Web  
**Purpose**: Fast, stable unit/integration tests for Android  
**Coverage**:
- Compose UI component rendering
- WebView container presence
- Loading indicator visibility
- Basic interaction flows

**Pros**:
- ✅ Fast execution (runs on JVM or instrumentation)
- ✅ Stable and well-maintained by Google
- ✅ Deep Compose integration
- ✅ Can capture device logs via UiAutomator
- ✅ Good CI/CD support

**Cons**:
- ❌ Android-only (no iOS coverage)
- ❌ Limited WebView DOM introspection
- ❌ Requires emulator/device for full WebView testing

**When to use**: Primary test suite for Android; run on every commit

---

### Layer 2: Maestro (Cross-platform E2E)
**Tool**: Maestro by mobile.dev  
**Purpose**: Simple, cross-platform E2E flows with screenshots  
**Coverage**:
- App launch and navigation
- WebView content visibility (accessibility tree)
- User flows across screens
- Screenshot capture for visual regression

**Pros**:
- ✅ Works on both Android and iOS
- ✅ Simple YAML-based test syntax
- ✅ Easy to write and maintain
- ✅ Built-in screenshot support
- ✅ No code compilation needed
- ✅ Good CI/CD support
- ✅ Can verify text visible on screen (including WebView content)

**Cons**:
- ❌ Limited WebView DOM access
- ❌ Cannot execute JavaScript in WebView
- ❌ Relies on accessibility tree (text must be visible)

**When to use**: Critical user journeys, smoke tests, cross-platform validation

---

### Layer 3: Appium (Deep WebView Validation)
**Tool**: Appium with UiAutomator2 (Android) / XCUITest (iOS)  
**Purpose**: Deep WebView DOM inspection and JavaScript execution  
**Coverage**:
- WebView context switching
- DOM element verification
- JavaScript execution in WebView
- Complex web interactions
- Device log collection

**Pros**:
- ✅ Full WebView DOM access
- ✅ Can execute JavaScript
- ✅ Cross-platform (Android + iOS)
- ✅ Can switch between NATIVE and WEBVIEW contexts
- ✅ Comprehensive log collection (adb logcat, iOS logs)

**Cons**:
- ❌ Complex setup and maintenance
- ❌ Slower than other solutions
- ❌ Requires Appium server
- ❌ More brittle tests
- ❌ Steeper learning curve

**When to use**: When you need to verify specific WebView DOM elements or execute JavaScript; selective use for critical WebView functionality

---

### Layer 4: Visual Regression Testing

#### Option A: Paparazzi (Android Component Screenshots)
**Purpose**: Fast JVM-level screenshots for Compose components  
**Coverage**: Individual Compose UI components (not WebView content)

**Pros**:
- ✅ Very fast (runs on JVM, no device needed)
- ✅ Deterministic screenshots
- ✅ Good for design system validation
- ✅ Easy CI/CD integration

**Cons**:
- ❌ Android-only
- ❌ Cannot capture WebView content
- ❌ Limited to Compose components

**When to use**: Design system components, UI consistency validation

#### Option B: Shot (Android Instrumentation Screenshots)
**Purpose**: On-device screenshot testing with view hierarchy control  
**Coverage**: Full Android screens including WebView rasterized content

**Pros**:
- ✅ Captures actual rendered output including WebView
- ✅ Facebook's screenshot testing approach
- ✅ Good IDE integration

**Cons**:
- ❌ Android-only
- ❌ Requires emulator/device
- ❌ Slower than Paparazzi

**When to use**: Full-screen visual regression including WebView content on Android

#### Option C: Maestro Screenshots (Cross-platform)
**Purpose**: Whole-screen captures during E2E flows  
**Coverage**: Any screen visible during Maestro test execution

**Pros**:
- ✅ Cross-platform (Android + iOS)
- ✅ Part of E2E flow
- ✅ Simple setup
- ✅ Can be stored in Git or uploaded to cloud

**Cons**:
- ❌ Less control than specialized tools
- ❌ Manual baseline management

**When to use**: E2E flow validation, cross-platform screenshot comparison

---

## Recommended Implementation Roadmap

### Phase 1: Foundation (Immediate)
**Goal**: Get fast feedback on Android builds

1. **Add Android Compose UI Tests**
   - Test WebView container renders
   - Test loading indicator displays/hides
   - Test basic navigation
   - Capture and verify logs

2. **Dependencies**:
   ```kotlin
   androidTestImplementation("androidx.compose.ui:ui-test-junit4")
   androidTestImplementation("androidx.test.ext:junit:1.1.5")
   androidTestImplementation("androidx.test:runner:1.5.2")
   androidTestImplementation("androidx.test.espresso:espresso-web:3.5.1")
   androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
   debugImplementation("androidx.compose.ui:ui-test-manifest")
   ```

3. **Test Examples**:
   - Smoke test: App launches and WebView renders
   - Loading test: Loading indicator appears then disappears
   - Log test: No fatal errors in logcat

**Timeline**: 1-2 days  
**Effort**: Low  
**Value**: High (fast feedback loop)

---

### Phase 2: Cross-platform (Next)
**Goal**: Validate critical flows on both Android and iOS

1. **Install Maestro**:
   ```bash
   brew tap mobile-dev-inc/tap
   brew install maestro
   ```

2. **Create Maestro Flows**:
   - `.maestro/01_smoke.yaml`: Launch app, verify WebView content visible
   - `.maestro/02_navigation.yaml`: Test back navigation
   - `.maestro/03_visual.yaml`: Take screenshots for baseline

3. **Test Examples**:
   - Assert "Grokipedia" text is visible on screen
   - Capture screenshot for visual comparison
   - Test system back button

**Timeline**: 1 day  
**Effort**: Low  
**Value**: High (cross-platform coverage with minimal effort)

---

### Phase 3: Deep WebView Validation (As Needed)
**Goal**: Verify WebView DOM elements and execute JavaScript

1. **Install Appium**:
   ```bash
   npm install -g appium @appium/doctor
   appium driver install uiautomator2
   appium driver install xcuitest
   ```

2. **Enable WebView Debugging** (in debug builds):
   ```kotlin
   if (BuildConfig.DEBUG) {
       WebView.setWebContentsDebuggingEnabled(true)
   }
   ```

3. **Create Appium Tests** (Python/JavaScript):
   - Switch to WEBVIEW context
   - Verify document.title
   - Query DOM for specific elements
   - Execute JavaScript

4. **Test Examples**:
   - Verify page title contains expected text
   - Check if specific DOM elements exist
   - Validate link targets

**Timeline**: 2-3 days  
**Effort**: Medium-High  
**Value**: Medium (needed only for specific WebView validations)

---

### Phase 4: Visual Regression (Optional)
**Goal**: Catch unintended UI changes

1. **For Compose Components**: Use Paparazzi
   - Fast, deterministic
   - Good for design system

2. **For Full Screens**: Use Maestro Screenshots or Shot
   - Maestro: Cross-platform
   - Shot: Android-only but more control

**Timeline**: 1-2 days  
**Effort**: Low-Medium  
**Value**: Medium (depends on UI stability requirements)

---

## CI/CD Integration Strategy

### GitHub Actions Workflows

#### Workflow 1: Build and Unit Tests
```yaml
name: Build and Test
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - uses: gradle/gradle-build-action@v2
      - name: Build and test
        run: ./gradlew assembleDebug testDebugUnitTest
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: composeApp/build/outputs/apk/debug/
```

#### Workflow 2: Android Instrumentation Tests
```yaml
name: Android Instrumentation Tests
on: [push, pull_request]
jobs:
  instrumentation:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Run instrumentation tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 35
          arch: x86_64
          script: |
            adb logcat > logcat.txt &
            ./gradlew connectedDebugAndroidTest
            pkill adb
      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: instrumentation-results
          path: |
            composeApp/build/reports/androidTests/
            logcat.txt
```

#### Workflow 3: Maestro E2E Tests (Android)
```yaml
name: Maestro E2E (Android)
on: [push, pull_request]
jobs:
  maestro-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Build APK
        run: ./gradlew assembleDebug
      - name: Run Maestro tests
        uses: mobile-dev-inc/action-maestro-cloud@v1
        with:
          api-key: ${{ secrets.MAESTRO_CLOUD_API_KEY }}
          app-file: composeApp/build/outputs/apk/debug/composeApp-debug.apk
          # Or run locally:
          # - uses: reactivecircus/android-emulator-runner@v2
          #   with:
          #     api-level: 35
          #     script: |
          #       curl -Ls "https://get.maestro.mobile.dev" | bash
          #       maestro test .maestro
```

#### Workflow 4: Maestro E2E Tests (iOS)
```yaml
name: Maestro E2E (iOS)
on: [push, pull_request]
jobs:
  maestro-ios:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup Xcode
        uses: maxim-lobanov/setup-xcode@v1
        with:
          xcode-version: latest-stable
      - name: Build iOS app
        run: |
          cd iosApp
          xcodebuild -scheme iosApp -sdk iphonesimulator -configuration Debug
      - name: Start simulator
        run: |
          xcrun simctl boot "iPhone 15"
      - name: Install Maestro
        run: |
          brew tap mobile-dev-inc/tap
          brew install maestro
      - name: Run Maestro tests
        run: maestro test .maestro -p ios
      - name: Upload screenshots
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: maestro-screenshots-ios
          path: .maestro/screenshots/
```

---

## Log Collection and Analysis

### Android Log Collection

#### During Tests (Compose UI Tests)
```kotlin
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

@Test
fun captureLogsTest() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    
    // Clear logs before test
    device.executeShellCommand("logcat -c")
    
    // Run test actions
    // ...
    
    // Capture logs
    val logs = device.executeShellCommand("logcat -d")
    
    // Assert no errors
    assertFalse(logs.contains("FATAL"))
    assertFalse(logs.contains("AndroidRuntime: FATAL EXCEPTION"))
}
```

#### In CI/CD
```bash
# Start logcat in background
adb logcat -v time > logcat.txt &
LOGCAT_PID=$!

# Run tests
./gradlew connectedDebugAndroidTest

# Stop logcat
kill $LOGCAT_PID

# Analyze logs
grep -E "FATAL|ERROR" logcat.txt
```

### iOS Log Collection

```bash
# In CI/CD
xcrun simctl spawn booted log stream --level=error > ios_logs.txt &
LOG_PID=$!

# Run tests
maestro test .maestro -p ios

# Stop logging
kill $LOG_PID

# Analyze logs
grep -E "error|crash" ios_logs.txt
```

---

## Testing Matrix

| Test Type | Tool | Android | iOS | WebView DOM | Speed | Setup Complexity | CI/CD |
|-----------|------|---------|-----|-------------|-------|------------------|-------|
| Unit/Integration | Compose Testing | ✅ | ❌ | ❌ | ⚡⚡⚡ | Low | ✅ |
| E2E Flows | Maestro | ✅ | ✅ | Limited | ⚡⚡ | Low | ✅ |
| WebView Deep | Appium | ✅ | ✅ | ✅ | ⚡ | High | ✅ |
| Visual (Components) | Paparazzi | ✅ | ❌ | ❌ | ⚡⚡⚡ | Low | ✅ |
| Visual (Screens) | Shot | ✅ | ❌ | ✅ | ⚡⚡ | Medium | ✅ |
| Visual (Cross-platform) | Maestro | ✅ | ✅ | ✅ | ⚡⚡ | Low | ✅ |
| Log Analysis | UiAutomator/Shell | ✅ | ✅ | N/A | ⚡⚡⚡ | Low | ✅ |

---

## Decision Matrix: When to Use Each Tool

### Use Compose UI Tests when:
- ✅ You need fast feedback on Android
- ✅ Testing Compose component behavior
- ✅ Verifying UI state changes
- ✅ Running tests frequently (every commit)

### Use Maestro when:
- ✅ You need cross-platform coverage
- ✅ Testing user flows end-to-end
- ✅ You want simple, maintainable tests
- ✅ Taking screenshots for visual regression
- ✅ Verifying visible text content (including WebView)

### Use Appium when:
- ✅ You need to verify specific WebView DOM elements
- ✅ You need to execute JavaScript in WebView
- ✅ You need to switch between native and web contexts
- ✅ You need detailed WebView state inspection

### Use Paparazzi when:
- ✅ Testing Compose components in isolation
- ✅ You want very fast screenshot tests
- ✅ You don't need WebView content validation

### Use Shot when:
- ✅ You need full-screen screenshots including WebView
- ✅ Android-only is acceptable
- ✅ You want Facebook's screenshot testing approach

---

## Conclusion

**Recommended starting point**:
1. **Implement Compose UI Tests** (Phase 1) - Fast feedback for Android
2. **Add Maestro flows** (Phase 2) - Cross-platform E2E coverage
3. **Consider Appium** (Phase 3) - Only if deep WebView validation needed
4. **Add visual regression** (Phase 4) - Based on stability requirements

This layered approach provides:
- ✅ Fast feedback (Compose tests)
- ✅ Cross-platform coverage (Maestro)
- ✅ Deep validation capability (Appium, when needed)
- ✅ Visual regression (multiple options)
- ✅ Log capture and analysis (built into all layers)
- ✅ CI/CD ready (all tools support automation)

**Total estimated effort**: 4-6 days for full implementation  
**Maintenance**: Low to Medium depending on test coverage
