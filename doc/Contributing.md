# Contributing to Homeal

## Development Setup

### Backend Setup
```bash
# Clone repository
git clone https://github.com/pdg-16-2025/homeal
cd homeal

# Setup server
cd server
go run *.go
```

### Android Setup
```bash
# Open Android Studio
# File > Open > homeal/android/
# Sync Gradle files
# Run on emulator/device
```

## Workflow
1. Create feature branch from `dev`
2. Develop and test locally
3. Create Pull Request
4. Code review
5. Merge to `main`

## Network Configuration
- Emulator: Uses `http://10.0.2.2:3000/`
- Physical device: Change to your local IP in `NetworkModule.kt`