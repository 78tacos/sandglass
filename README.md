# Sandglass

A Material You **focus / sand-timer** for Android. Set a duration, start focusing, and watch the hourglass drain. Offline-only: no network, no accounts, no ads, no tracking.

Sandglass is a small portfolio app — polished enough to install and use, intentionally not a product platform.

## Features

- Duration presets (5 / 10 / 15 / 25 / 45 minutes)
- Start, pause, resume, and reset
- Animated hourglass: upper chamber empties, lower chamber fills, grains fall while running
- Optional gentle haptic tick each second (`CLOCK_TICK`)
- Light and dark themes, with Material You dynamic color on Android 12+
- Works fully offline

## Install

Download an APK from [Releases](https://github.com/78tacos/sandglass/releases):

- Any phone or emulator: `sandglass-*-android-universal-debug.apk`
- 64-bit ARM phones: `sandglass-*-android-arm64-v8a-debug.apk` (smaller)

On the device, allow install from unknown sources for your browser or Files app, then open the APK. These GitHub builds are **debug-signed for sideloading** (not Play Store / Play App Signing). They work on Android 8.0+ (`minSdk` 26).

## Requirements

- [Android Studio](https://developer.android.com/studio) **Quail 2+** (2026.1.2+) — or any IDE release that supports **AGP 9.3**
- JDK 17 or newer (Studio’s bundled JDK is fine)
- Android SDK **API 37** (`compileSdk`) — Studio will prompt to install it if missing
- Android emulator or a device running **API 26+** (Android 8.0)

## Open and run

1. Clone the repository:

   ```bash
   git clone https://github.com/78tacos/sandglass.git
   cd sandglass
   ```

2. In Android Studio, choose **File → Open** and select the `sandglass` folder (the directory that contains `settings.gradle.kts`).
3. Wait for Gradle sync to finish. Studio will create `local.properties` with your SDK path.
4. Pick an emulator or a plugged-in device in the run configuration toolbar.
5. Click **Run** (the green triangle), or press `Shift+F10` / `Control+R`.

The launcher icon is an hourglass on a dark sand-colored adaptive icon. Grant no extra permissions — Sandglass does not request internet or notifications.

### Command line

```bash
./gradlew assembleDebug
```

ABI splits write a universal APK plus per-ABI APKs under `app/build/outputs/apk/debug/`. Install the universal build with:

```bash
adb install -r app/build/outputs/apk/debug/app-universal-debug.apk
```

## Tests

Unit tests cover countdown math (start / pause / reset / finish) and remaining-time formatting. They do not drive the Compose UI.

```bash
./gradlew test
```

HTML reports land in `app/build/reports/tests/testDebugUnitTest/`.

## Screenshots

Capture light and dark frames after you have an emulator running:

1. Start a **Pixel 6 / API 34+** AVD (or any recent device).
2. Open Sandglass, select **25m**, leave it idle — that is the hero frame.
3. Start the timer for a second shot with sand falling.
4. Toggle the device dark theme (**Settings → Display**) and repeat.

From a terminal:

```bash
adb exec-out screencap -p > docs/screenshots/idle-light.png
```

Drop PNGs in `docs/screenshots/` and embed them here, for example:

```markdown
![Idle, light](docs/screenshots/idle-light.png)
![Running, dark](docs/screenshots/running-dark.png)
```

Android Studio’s **Running Devices** window also has a camera button that saves a screenshot to your desktop.

## Project layout

```
app/src/main/kotlin/com/tacos78/sandglass/
  timer/          FocusTimer, formatting, and snapshot types (no Android APIs)
  ui/             Compose screen, hourglass Canvas, Material 3 theme
  data/           DataStore toggle for haptic ticks
  SandglassViewModel.kt
  MainActivity.kt
app/src/test/     JUnit tests for timer logic
```

`minSdk` is 26, `compileSdk` is 37, `targetSdk` is 36. Kotlin + Jetpack Compose + Material 3.

## Privacy

The app declares **no `INTERNET` permission** and ships no analytics or ad SDKs. The haptic preference is stored on-device with Jetpack DataStore. The timer runs while the app is visible; v1 is not a background alarm.

## License

[MIT](LICENSE) © 2026 78tacos
