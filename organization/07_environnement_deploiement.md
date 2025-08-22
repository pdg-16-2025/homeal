# Setting Up a Deployment Environment

## Overview

Development environment for the Homeal Android application supporting **multiple platforms**:
- **WSL2**: Windows Subsystem for Linux with X11 forwarding
- **Native Linux**: Direct Ubuntu/Debian installation

**Stack**: Ubuntu 22.04 + Android Studio + Git + Gradle

**Benefits**: Linux-native development, unified Git workflow, professional Android IDE

## Installation

### Prerequisites

#### For WSL2 (Windows users)
- Windows 10/11 with WSL2
- 8GB RAM minimum

#### For Native Linux
- Ubuntu 22.04+ or Debian-based distribution
- 8GB RAM minimum
- Desktop environment (GNOME, KDE, etc.)

### System Setup

#### WSL2 Setup (Windows users only)
```bash
# Install WSL2 with Ubuntu
wsl --install -d Ubuntu-22.04

# Update system packages
sudo apt update && sudo apt upgrade -y
```

#### Native Linux Setup
```bash
# Update system packages (Ubuntu/Debian)
sudo apt update && sudo apt upgrade -y
```

### Install Development Dependencies

```bash
# Essential packages
sudo apt install -y wget unzip openjdk-11-jdk git

# X11 utilities for GUI support
sudo apt install -y x11-apps x11-xserver-utils
```

### Android Studio
```bash
# Download Android Studio
cd /tmp
wget https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2024.2.1.11/android-studio-2024.2.1.11-linux.tar.gz

# Extract to /opt/
sudo tar -xzf android-studio-2024.2.1.11-linux.tar.gz -C /opt/

# Set ownership and permissions
sudo chown -R $USER:$USER /opt/android-studio

# Create global command
sudo ln -sf /opt/android-studio/bin/studio.sh /usr/local/bin/android-studio
```

#### Install VcXsrv (WSL2 users only)
1. Download from: https://sourceforge.net/projects/vcxsrv/
2. Install and launch **XLaunch**
3. Configuration:
   - **Display**: 0
   - **Multiple windows**
   - **Start no client**  
   - ✅ **Disable access control** (CRITICAL)

### Configure Display

#### For WSL2:
```bash
echo 'export DISPLAY=$(route.exe print | grep 0.0.0.0 | head -1 | awk "{print \$4}"):0.0' >> ~/.bashrc
```

#### For Native Linux:
```bash
# Display is automatically configured, no action needed
echo "DISPLAY is: $DISPLAY"
```

### Launch Script

#### For WSL2:
```bash
cat > ~/start-android-studio.sh << 'EOF'
#!/bin/bash
export DISPLAY=$(route.exe print | grep 0.0.0.0 | head -1 | awk '{print $4}'):0.0
export ANDROID_HOME=$HOME/Android/Sdk
cd ~/path/to/your/project
/opt/android-studio/bin/studio.sh &
EOF
chmod +x ~/start-android-studio.sh
```

#### For Native Linux:
```bash
cat > ~/start-android-studio.sh << 'EOF'
#!/bin/bash
export ANDROID_HOME=$HOME/Android/Sdk
cd ~/path/to/your/project
/opt/android-studio/bin/studio.sh &
EOF
chmod +x ~/start-android-studio.sh
```

## Usage

### For WSL2:
1. Start VcXsrv on Windows
2. Run: `~/start-android-studio.sh`
3. Open project: `/path/to/your/project/`

### For Native Linux:
1. Run: `~/start-android-studio.sh`
2. Open project: `/path/to/your/project/`

## Development Workflow

```bash
# Build project
./gradlew build

# Generate APK
./gradlew assembleDebug

# Run tests
./gradlew test
```

## Troubleshooting

### WSL2-specific issues:
- **X11 issues**: Verify VcXsrv is running with "Disable access control"
- **Git permissions**: Set `git config core.filemode false`

### Common issues (all platforms):
- **Build issues**: Run `./gradlew clean` then rebuild
- **Java issues**: Verify `java -version` shows Java 11+
- **Android Studio won't start**: Check `/opt/android-studio/bin/studio.sh` exists