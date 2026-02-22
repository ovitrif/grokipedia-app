# Android Instrumentation Tests

This directory contains Android instrumentation tests for the Grokipedia app using Jetpack Compose Testing framework.

## Test Suites

### 1. AppSmokeTest.kt

**Purpose**: Basic smoke test to verify app launches and WebView is displayed.

**Tests**:

- `appLaunches_andWebViewIsDisplayed()`: Verifies the app launches without crashing and the WebView container is
  visible.

### 2. LoadingIndicatorTest.kt

**Purpose**: Tests for loading indicator behavior (extensible for future enhancements).

**Tests**:

- `webViewContainer_isAlwaysPresent()`: Verifies WebView container exists even during loading.

**Note**: Contains commented-out test for loading indicator visibility that can be enabled when loading indicator gets a
testTag.

### 3. LogAnalysisTest.kt

**Purpose**: Demonstrates log capture and analysis capabilities.

**Tests**:

- `app_doesNotCrashOnLaunch()`: Verifies no fatal exceptions occur during app launch.
- `app_logsNoErrorsDuringWebViewLoad()`: Checks for errors during WebView initialization.
- `logcat_canBeCaptured()`: Demonstrates log capture capability.
- `webView_loadingDoesNotProduceErrors()`: Verifies WebView loading doesn't produce fatal errors.

## Running Tests

### From Command Line

```bash
# Run all instrumentation tests
./gradlew :composeApp:connectedDebugAndroidTest

# Run specific test class
./gradlew :composeApp:connectedDebugAndroidTest --tests "io.github.grokipedia.AppSmokeTest"

# Run specific test method
./gradlew :composeApp:connectedDebugAndroidTest --tests "io.github.grokipedia.AppSmokeTest.appLaunches_andWebViewIsDisplayed"
```

### From Android Studio

1. Right-click on the test class or method
2. Select "Run 'TestName'"

### Prerequisites

- An Android emulator must be running OR a physical device connected
- The emulator/device must have:
    - API level 24+ (minSdk)
    - Internet connectivity (for WebView to load content)

## Test Output

### Success

- Tests will pass with green checkmarks
- JUnit reports available at: `composeApp/build/reports/androidTests/connected/`

### Failure

- Failed tests show red X with failure message
- Logcat output captured in test reports
- Screenshots captured on failure (when configured)

## Viewing Test Reports

After running tests:

```bash
# Open HTML report
open composeApp/build/reports/androidTests/connected/index.html

# Or on Linux
xdg-open composeApp/build/outputs/reports/androidTests/connected/index.html
```

## CI/CD Integration

These tests are designed to run in CI/CD pipelines:

```yaml
# GitHub Actions example
- name: Run instrumentation tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 35
    arch: x86_64
    script: |
      adb logcat > logcat.txt &
      ./gradlew connectedDebugAndroidTest
      pkill adb
```

## Log Collection

Tests use `UiDevice.executeShellCommand()` to:

- Clear logs before tests (`logcat -c`)
- Capture logs after tests (`logcat -d`)
- Filter logs by tag or severity
- Analyze logs for errors

Example:

```kotlin
val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
device.executeShellCommand("logcat -c")
// ... run test actions ...
val logs = device.executeShellCommand("logcat -d -s YourTag:*")
```

## Test Tags

The app uses these test tags for UI testing:

- `webview`: The main WebView component

To add more test tags:

```kotlin
Modifier.testTag("your_tag_name")
```

## Troubleshooting

### Tests fail with "No instrumentation runner found"

**Solution**: Ensure `testInstrumentationRunner` is set in `build.gradle.kts`:

```kotlin
defaultConfig {
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
}
```

### Tests timeout waiting for WebView

**Solution**: Increase timeout or check internet connectivity:

```kotlin
composeTestRule.waitForIdle()
// or
Thread.sleep(5000) // Give WebView time to load
```

### Cannot capture logs

**Solution**: Ensure UiAutomator dependency is added:

```kotlin
androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
```

### Emulator too slow

**Solution**:

- Use x86_64 architecture
- Enable hardware acceleration
- Increase emulator RAM in AVD settings

## Best Practices

1. **Keep tests focused**: Each test should verify one specific behavior
2. **Use meaningful names**: Test names should describe what they verify
3. **Clean up logs**: Clear logs before each test to avoid pollution
4. **Wait appropriately**: Use `waitForIdle()` or explicit waits for async operations
5. **Handle flakiness**: Add retries for network-dependent tests if needed
6. **Document assumptions**: Comment any timing assumptions or external dependencies

## Future Enhancements

Potential test additions:

- [ ] WebView back navigation tests
- [ ] Error page handling tests
- [ ] Network connectivity loss/recovery tests
- [ ] WebView JavaScript execution tests (requires Espresso Web)
- [ ] Screenshot comparison tests
- [ ] Performance benchmarking
- [ ] Accessibility tests

## Resources

- [Compose Testing Documentation](https://developer.android.com/jetpack/compose/testing)
- [Espresso Web](https://developer.android.com/training/testing/espresso/web)
- [UiAutomator](https://developer.android.com/training/testing/other-components/ui-automator)
- [Android Testing Samples](https://github.com/android/testing-samples)
