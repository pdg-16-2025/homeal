# Description of Technical Choices

## Overview

The technical choices for the Homeal application are oriented towards **simplicity**
and **maintainability**. All technical decisions prioritize local autonomy and robustness.

## Main Technical Stack

### Platform and Language
- **Platform**: Native Android
- **Language**: Kotlin
- **Minimum SDK**: Android API 21+ (Android 5.0)
- **Justification**:
  - Modern and concise Kotlin for Android development
  - Optimal native performance
  - Mature and stable Android ecosystem
  - Guaranteed long-term support

### User Interface
- **Framework**: Jetpack Compose
- **Justification**:
  - Modern UI framework recommended by Google
  - Declarative and reactive
  - Native integration with Android ecosystem
  - Optimized performance
  - Acceptable learning curve

### Database
- **Engine**: SQLite
- **ORM**: Direct access (no Room initially)
- **Justification**:
  - Native integration in Android
  - Lightweight and simple on the server
  - Optimal performance for local queries
  - Reduced size and minimal memory consumption
  - Proven maturity and stability

## Specific Technical Features

### Barcode Scanner
- **Technology**: CameraX + ML Kit
- **Justification**:
  - CameraX for modern camera management
  - ML Kit for robust barcode recognition
  - Native Android integration
  - Optimal performance

### Image Management
- **Storage**: Local only
- **Format**: Static compressed images

## Development Tools

### Development Environment
- **IDE**: Android Studio (latest stable version)
- **Build System**: Gradle
- **Justification**:
  - Official Android environment
  - Complete ecosystem integration
  - Advanced debugging and profiling
  - Latest features support

### Version Management
- **VCS**: Git
- **Hosting**: GitHub
- **Workflow**: Adapted Git Flow
- **Justification**:
  - Industry standard
  - Excellent CI/CD integration
  - Facilitated team collaboration
