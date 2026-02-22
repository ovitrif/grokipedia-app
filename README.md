# Grokipedia App

Unofficial app for Grokipedia, so you can forget about Wokipedia!

A Compose Multiplatform mobile application that provides a native wrapper for the Grokipedia website, available on both Android and iOS platforms.

## Features

- 🌐 Full WebView integration displaying [Grokipedia.org](https://grokipedia.org)
- 📱 Native Android and iOS support
- 🎨 Material 3 design with loading indicators
- ⬅️ Back button navigation support
- 🚀 Built with latest Compose Multiplatform (1.9.4)
- 📦 Uses Compose BOM for consistent dependency versioning

## Technology Stack

- **Compose Multiplatform**: 1.9.4
- **Kotlin**: 2.2.20
- **Android Gradle Plugin**: 8.5.2
- **WebView Library**: [compose-webview-multiplatform](https://github.com/KevinnZou/compose-webview-multiplatform) 2.0.3
- **Compose BOM**: 2025.10.01

---

This is a Kotlin Multiplatform project targeting Android and iOS.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.


## Testing

This project uses a multi-layered testing strategy including Unit Tests, Android Instrumentation Tests, and Maestro End-to-End tests.

For detailed instructions on running tests, see [Testing Guide](docs/TESTING_GUIDE.md).

### Quick Start (E2E)
To run the core functionalities test:
```bash
maestro test .maestro/04_core_functionalities.yaml
```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
