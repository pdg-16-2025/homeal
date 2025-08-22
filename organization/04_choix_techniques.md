# Description of Technical Choices

## Overview

The technical choices for the Homeal application are oriented towards **simplicity**, **offline performance**, and **maintainability**. All technical decisions prioritize local autonomy and robustness.

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
  - Optimal performance for local queries
  - Reduced size and minimal memory consumption
  - No network dependency
  - Proven maturity and stability

## Architecture Pattern

### Main Pattern: MVC (Model-View-Controller)
- **Justification**:
  - Clear separation of responsibilities
  - Familiar and well-documented pattern
  - Implementation simplicity for a small team
  - Facilitates unit testing
  - Evolutionary architecture

### Alternative Considered: MVVM
- **Rejected for**:
  - Additional complexity not justified for MVP
  - Higher learning curve
  - MVC pattern sufficient for current needs

## Offline-First Approach

### Base Principle
- **100% Offline**: No network dependency for basic functionality
- **Static data**: Recipes and OpenFoodFacts preloaded at installation
- **Dynamic data**: Local storage exclusively

### Advantages
- **Performance**: No network latency
- **Reliability**: Guaranteed functionality without connectivity
- **Costs**: No server or API fees
- **Simplicity**: Reduced architecture and fewer failure points

## Data Management

### Product Data Source
- **OpenFoodFacts**: Integrated static dataset
- **Format**: Pre-populated SQLite database
- **Subset**: Filtered and optimized data (essential columns)
- **Justification**:
  - Free and complete database
  - Verified data quality
  - Multi-language support
  - No real-time API dependency

### Data Structure
- **Relational tables**: Normalized structure
- **Embedded JSON**: For ingredient lists (first iteration)
- **Indexing**: Optimized indexes on frequently queried columns
- **Migration**: Manual migration scripts

## Specific Technical Features

### Barcode Scanner
- **Technology**: CameraX + ML Kit
- **Justification**:
  - CameraX for modern camera management
  - ML Kit for robust barcode recognition
  - Native Android integration
  - Optimal performance

### Image Management
- **Storage**: Local only (first phase)
- **Format**: Static compressed images
- **Future**: Optional network loading in phase 2

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

## Detailed Architecture Choices

### Modularity
- **Approach**: Simple package structure
- **Separation**:
  - `ui/`: Jetpack Compose components
  - `data/`: Database access
  - `domain/`: Business logic
  - `utils/`: Common utilities

### Dependency Management (for exemple)
```kotlin
dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.x.x'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.x.x'
    
    // UI
    implementation 'androidx.compose.ui:ui:1.x.x'
    implementation 'androidx.compose.material3:material3:1.x.x'
    
    // Database
    implementation 'androidx.sqlite:sqlite-ktx:2.x.x'
    
    // Camera & Barcode
    implementation 'androidx.camera:camera-camera2:1.x.x'
    implementation 'com.google.mlkit:barcode-scanning:17.x.x'
}
```

## Choice Justifications

### SQLite vs Room
- **Choice**: Direct SQLite
- **Justification**:
  - Total control over queries
  - Optimal performance without abstraction
  - Simplicity for MVP
  - Possible migration to Room later

### MVC vs MVVM/Clean Architecture
- **Choice**: MVC
- **Justification**:
  - Small team with time constraints
  - Complexity appropriate to project
  - Familiar and quick-to-implement pattern
  - Possible evolution to more complex architectures

### Offline vs Online-First
- **Choice**: Offline-First
- **Justification**:
  - Food waste reduction goal independent of connectivity
  - Reliable user experience
  - Zero infrastructure costs
  - Development and maintenance simplicity

## Trade-offs and Limitations

### Assumed Limitations
- **Product data**: No real-time OpenFoodFacts updates
- **Synchronization**: No cross-device sync (phase 1)
- **Images**: Quality limited by local storage

### Evolution Plans
- **Phase 2**: Optional cloud synchronization
- **Phase 3**: Periodic product data updates
- **Phase 4**: Advanced features (nutrition, AI)

## Stack Evaluation

### Strengths
- Optimal native performance
- Guaranteed offline reliability
- Controlled complexity
- Mature and stable ecosystem
- Acceptable learning curve

### Points of Attention
- Application size (OpenFoodFacts data)
- Manual data updates
- No cross-device synchronization
