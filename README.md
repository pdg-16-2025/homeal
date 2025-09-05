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

### Running the Backend Server locally
```bash
cd server
go run *.go
# Server runs on http://localhost:3000
```

### Running the Backend Server with Docker
```bash
cd server
docker build -t homeal-server .
docker run -p 3000:3000 -p 80:80 homeal-server
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

> [!WARNING]
> For the android app connect to your server, you need to change the ip address
> in [NetworkModule.kt](https://github.com/pdg-16-2025/homeal/blob/main/android/app/src/main/java/com/example/homeal_app/data/remote/NetworkModule.kt)
> at `private const val BASE_URL = "your-ip-address:3000//"` and in
> [network_security_config.xml](https://github.com/pdg-16-2025/homeal/blob/main/android/app/src/main/res/xml/network_security_config.xml)
> add `<domain includeSubdomains="true">your-ip-address</domain>`, else, the app
> will connect to our public server.

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


