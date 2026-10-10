# Sandglass

Focus timer for Android, drawn as an hourglass. Set a length, start it, and the sand runs out. Nothing leaves the device.

## Features

- 5, 10, 15, 25, and 45 minute presets
- Start, pause, resume, and reset
- Animated hourglass while the timer is running
- Optional haptic tick once a second
- Light and dark themes, and Material You dynamic color on Android 12+

## Install

[Releases](https://github.com/78tacos/sandglass/releases) include debug-signed APKs for sideloading. They run on Android 8.0 and later. Allow install from unknown sources, then open the APK.

- `sandglass-*-android-universal-debug.apk` for any phone or emulator
- `sandglass-*-android-arm64-v8a-debug.apk` for 64-bit ARM (smaller)

These are not Play App Signing builds.

## Building

JDK 17, Android SDK 37, and Android Studio Quail 2 (2026.1.2) or another IDE that supports Android Gradle Plugin 9.3. `minSdk` is 26 and `targetSdk` is 36.

Open the folder that contains `settings.gradle.kts`, or build from a terminal:

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-universal-debug.apk
```

Per-ABI APKs land next to the universal one under `app/build/outputs/apk/debug/`.

## Tests

```bash
./gradlew test
```

Unit tests cover countdown math and the clock format. They do not launch the UI. HTML reports are written to `app/build/reports/tests/testDebugUnitTest/`.

## Source

```
app/src/main/kotlin/com/tacos78/sandglass/
  timer/   FocusTimer, formatting, snapshots
  ui/      Compose screen, hourglass, theme
  data/    haptic preference (DataStore)
  SandglassViewModel.kt
  MainActivity.kt
app/src/test/  JUnit tests for the timer
```

## Privacy

No `INTERNET` permission, no analytics, and no ads. The haptic switch is stored on the device. The timer runs only while Sandglass is open.

## License

[MIT](LICENSE) © 2026 78tacos
