# Heating Control - Android App

## Overview
This app lets you control a heating unit via SMS. It sends commands and displays responses from the unit.

## Features
- **ON** (Green button) → Sends `#01#`
- **OFF** (Red button) → Sends `#02#`
- **STATUS** (Grey button) → Sends `#07#`
- Displays response in format:
  ```
  Main unit: ON/OFF XXC
  "Hab1": ON/OFF YYC
  ```
- Editable phone number saved between sessions

## How to Build

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11 or later
- Android SDK (API 21+)

### Steps

1. **Open the project in Android Studio:**
   - File → Open → select the `HeatingControl` folder

2. **Let Gradle sync** (it will download dependencies automatically)

3. **Build the APK:**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - The APK will be in: `app/build/outputs/apk/debug/app-debug.apk`

4. **Install on your Android device:**
   - Enable "Install from unknown sources" in Android settings
   - Transfer the APK to your device and tap to install
   - OR use ADB: `adb install app/build/outputs/apk/debug/app-debug.apk`

### Or build from command line:
```bash
./gradlew assembleDebug
```

## Permissions Required
The app will request these on first launch:
- **SEND_SMS** - to send commands
- **RECEIVE_SMS** - to receive responses
- **READ_SMS** - to read response content

## Notes
- The phone number is saved automatically when you leave the input field or press a button
- Only messages from the configured number are displayed
- The response display updates automatically when a matching SMS arrives
