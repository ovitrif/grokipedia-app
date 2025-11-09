# Testing Guide - Grokipedia App

## Running Tests on Specific Devices

By default, `connectedAndroidTest` runs on **all connected devices**. This can be problematic when you have both emulators and physical devices connected.

### Option 1: Run on Specific Device by Serial

```bash
# List connected devices first
adb devices

# Example output:
# emulator-5554    device
# 48201FDAP00587   device

# Run tests only on emulator
ANDROID_SERIAL=emulator-5554 ./gradlew :composeApp:connectedDebugAndroidTest

# Or specify the serial directly
adb -s emulator-5554 shell ...
```

### Option 2: Disconnect Physical Devices Before Testing

```bash
# Physically disconnect or disable via adb
adb disconnect <device_serial>

# Or kill the adb server and restart with only emulator
adb kill-server
adb start-server
```

### Option 3: Use Device-Specific Gradle Tasks

Gradle automatically creates tasks for each connected device:

```bash
# List all available test tasks
./gradlew tasks --all | grep "connected"

# Run on specific device (example)
./gradlew :composeApp:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=io.github.grokipedia.AppSmokeTest
```

### Option 4: Specify in gradle.properties

Create or edit `gradle.properties`:

```properties
# Only use emulators for testing
android.testOptions.devices=emulator-5554
```

## Recommended Approach for Development

**Best practice**: Only run emulator during testing

```bash
# 1. Check connected devices
adb devices

# 2. If physical device is connected, disconnect it
# (Physically unplug or use adb disconnect)

# 3. Verify only emulator is connected
adb devices
# Should show only: emulator-5554    device

# 4. Run tests
./gradlew :composeApp:connectedDebugAndroidTest
```

## Quick Test Commands

```bash
# Run all instrumentation tests (on connected emulator only)
./gradlew :composeApp:connectedDebugAndroidTest

# Run specific test class
./gradlew :composeApp:connectedDebugAndroidTest \
  --tests "io.github.grokipedia.AppSmokeTest"

# Run specific test method
./gradlew :composeApp:connectedDebugAndroidTest \
  --tests "io.github.grokipedia.AppSmokeTest.appLaunches_andWebViewIsDisplayed"

# Run with detailed output
./gradlew :composeApp:connectedDebugAndroidTest --info

# Clean and rerun tests
./gradlew :composeApp:cleanConnectedDebugAndroidTest :composeApp:connectedDebugAndroidTest
```

## Test Results Location

After running tests, results are available at:

```bash
# HTML Report (opens in browser)
open composeApp/build/reports/androidTests/connected/debug/index.html

# XML Results
ls composeApp/build/outputs/androidTest-results/connected/debug/

# Logcat output (if captured)
cat composeApp/build/outputs/androidTest-results/connected/debug/*.log
```

## CI/CD Considerations

In CI/CD (GitHub Actions, etc.), you typically:
1. Start a single emulator
2. Wait for it to be ready
3. Run tests (only that emulator is connected)

Example GitHub Actions snippet:
```yaml
- name: Run instrumentation tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 35
    arch: x86_64
    script: ./gradlew :composeApp:connectedDebugAndroidTest
```

The emulator runner ensures only one device is active.

## Troubleshooting

### Tests run on physical device when I don't want them to

**Solution**: Disconnect physical device before running tests
```bash
adb devices  # Check what's connected
# Unplug physical device
adb devices  # Verify only emulator remains
./gradlew :composeApp:connectedDebugAndroidTest
```

### Tests fail on physical device but pass on emulator

**Reason**: Physical devices may have different:
- Android versions
- Screen sizes/densities
- Performance characteristics
- Background processes

**Solution**: Focus on emulator for automated tests, use physical devices for manual testing.

### "No connected devices" error

**Solution**: Start emulator first
```bash
# Start specific emulator
emulator -avd Pixel_8_35 -no-snapshot &

# Wait for it to boot
adb wait-for-device

# Verify it's ready
adb shell getprop sys.boot_completed  # Should return "1"

# Run tests
./gradlew :composeApp:connectedDebugAndroidTest
```

## Test Execution Times (Reference)

Based on our test run on Pixel_8_35 emulator:
- AppSmokeTest: ~1.8s
- LoadingIndicatorTest: ~0.8s  
- LogAnalysisTest (4 tests): ~18s total
- **Total suite**: ~21s

Plan accordingly for CI/CD timeouts.
