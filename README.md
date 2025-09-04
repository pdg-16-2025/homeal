# Homeal

Android app to plan meals with what's already in your fridge.

## Features
- Track fridge ingredients (manual + barcode scan)
- Get recipe recommendations and plan a weekly meal calendar
- Generate a smart shopping list from planned meals
- Reduce waste with expiration‑based recommendations

## Quick Start

### Prerequisites
- Go 1.21+
- Android Studio
- SQLite3

### Running the Backend Server
```bash
cd server
go run *.go
# Server runs on http://localhost:3000
```

### Running the Android App
#### Option 1: APK Installation
```bash
# Generate APK from terminal
cd android
./gradlew assembleDebug

# APK location
app/build/outputs/apk/debug/app-debug.apk

# Copy to desktop for easy transfer
cp app/build/outputs/apk/debug/app-debug.apk ~/Desktop/homeal.apk
```

**Actual APK is available on Github**

**Install on your device:**
1. Transfer `homeal.apk` to your phone (USB, email, cloud storage)
2. Open the APK file on your phone
3. Allow installation from unknown sources if prompted
4. Install and launch the app

#### Option 2: Android Studio Development
1. Open `android/` in Android Studio
2. Sync Gradle files
3. Run on emulator or connected device

## Documentation
- [Architecture](doc/architecture.md)
- [API Documentation](doc/rest-api.md)
- [Database Schema](doc/db_schemas/)
- [CI/CD Setup](doc/CI-CD-README.md)


