# Maestro E2E Tests for Grokipedia

This directory contains Maestro test flows for cross-platform end-to-end testing of the Grokipedia app.

## What is Maestro?

Maestro is a simple, declarative mobile UI testing framework that works on both Android and iOS. Tests are written in YAML and can verify UI interactions, take screenshots, and validate user flows without needing to compile code.

## Test Flows

### 01_smoke.yaml
**Purpose**: Quick smoke test to verify app launches and basic functionality.

**What it tests**:
- App launches without crashing
- WebView loads content
- "Grokipedia" text is visible

**Runtime**: ~5-10 seconds

### 02_navigation.yaml
**Purpose**: Test navigation and back button behavior.

**What it tests**:
- App navigation flow
- System back button functionality
- App state after navigation

**Runtime**: ~10-15 seconds

### 03_visual_regression.yaml
**Purpose**: Capture screenshots for visual regression testing.

**What it tests**:
- Creates baseline screenshots
- Captures different scroll positions
- Can be compared over time to detect UI changes

**Runtime**: ~15-20 seconds

## Running Tests

### Prerequisites

1. **Install Maestro CLI**:
   ```bash
   brew tap mobile-dev-inc/tap
   brew install maestro
   ```

2. **Start a device**:
   - For Android: Start emulator or connect physical device
   - For iOS: Start simulator

### Run All Tests

```bash
# Android
maestro test .maestro

# iOS (requires built iOS app)
maestro test .maestro --platform ios
```

### Run Specific Test

```bash
# Run smoke test only
maestro test .maestro/01_smoke.yaml

# Run with specific app file
maestro test .maestro/01_smoke.yaml --app-file composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

### Run on Specific Device

```bash
# List connected devices
maestro test --list-devices

# Run on specific Android device
maestro test .maestro --device emulator-5554

# Run on specific iOS simulator
maestro test .maestro --device "iPhone 15"
```

## Screenshots

Screenshots are automatically saved to `.maestro/screenshots/` directory (gitignored by default).

To keep screenshots for baseline comparison:
```bash
# Create baseline screenshots directory
mkdir -p .maestro/screenshots/baseline

# After first successful run, copy screenshots as baseline
cp .maestro/screenshots/*.png .maestro/screenshots/baseline/

# On subsequent runs, compare new screenshots to baseline
# (Manual comparison or use image diff tools)
```

## Platform-Specific Notes

### Android
- Works with any Android device or emulator (API 21+)
- No special setup required
- Can test on physical devices via USB or wireless debugging

### iOS
- Requires macOS
- Requires Xcode and iOS Simulator
- Physical device testing requires additional setup (provisioning profiles, etc.)

## Continuous Integration

### GitHub Actions Example

```yaml
name: Maestro E2E Tests

on: [push, pull_request]

jobs:
  maestro-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Build APK
        run: ./gradlew assembleDebug
        
      - name: Run Maestro tests
        uses: mobile-dev-inc/action-maestro-cloud@v1
        with:
          api-key: ${{ secrets.MAESTRO_CLOUD_API_KEY }}
          app-file: composeApp/build/outputs/apk/debug/composeApp-debug.apk
          
      # Or run locally on GitHub's emulator:
      - uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 35
          arch: x86_64
          script: |
            curl -Ls "https://get.maestro.mobile.dev" | bash
            export PATH="$HOME/.maestro/bin:$PATH"
            maestro test .maestro
```

### Local CI Simulation

```bash
# Build APK
./gradlew :composeApp:assembleDebug

# Start emulator (if not already running)
emulator -avd Pixel_8_35 -no-snapshot &
adb wait-for-device

# Run Maestro tests
maestro test .maestro

# Check exit code
echo $?  # Should be 0 if all tests passed
```

## Debugging Tests

### View Test Execution in Real-Time

Maestro shows visual feedback during test execution in the terminal.

### Failed Test Investigation

When a test fails:
1. Check the screenshot taken at the failure point
2. Review the Maestro log output
3. Run test with `--debug-output` flag for more details:
   ```bash
   maestro test .maestro/01_smoke.yaml --debug-output
   ```

### Common Issues

#### "App did not launch"
- Ensure app is built and APK exists
- Check applicationId matches in the flow
- Verify device is connected: `adb devices`

#### "Element not found: Grokipedia"
- Content may not have loaded yet
- Increase waitForAnimationToEnd timeout
- Check if WebView is displaying content correctly
- Verify internet connectivity on device

#### "Timeout waiting for animation"
- Increase timeout in the flow
- Check if WebView is stuck loading
- Verify network connectivity

## Best Practices

1. **Keep flows simple**: One flow should test one user journey
2. **Use meaningful names**: Screenshot names should describe what they show
3. **Add comments**: YAML comments explain what each step does
4. **Wait appropriately**: Use `waitForAnimationToEnd` after actions that trigger animations
5. **Handle flaky tests**: Add retries for network-dependent assertions
6. **Screenshot strategically**: Capture key states for visual validation

## Extending Tests

To add new test flows:

1. Create a new `.yaml` file in `.maestro/`
2. Start with `appId: io.github.grokipedia`
3. Add test steps (see [Maestro documentation](https://maestro.mobile.dev))
4. Test locally before committing
5. Update this README with the new flow description

## Resources

- [Maestro Documentation](https://maestro.mobile.dev)
- [Maestro CLI Reference](https://maestro.mobile.dev/cli/cli-reference)
- [Maestro Cloud](https://cloud.mobile.dev) - For hosted test execution
- [Maestro GitHub](https://github.com/mobile-dev-inc/maestro)
