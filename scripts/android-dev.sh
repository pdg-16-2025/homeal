#!/bin/bash

# Homeal Android Development Helper Script

set -e

ANDROID_DIR="android"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
print_header() {
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE} $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Navigate to android directory
cd "$PROJECT_ROOT/$ANDROID_DIR"

# Function to show usage
show_usage() {
    echo "Homeal Android Development Helper"
    echo ""
    echo "Usage: $0 <command>"
    echo ""
    echo "Available commands:"
    echo "  test              Run unit tests"
    echo "  test-all          Run all tests (unit + instrumented)"
    echo "  build             Build debug APK"
    echo "  build-release     Build release APK"
    echo "  lint              Run lint checks"
    echo "  clean             Clean build artifacts"
    echo "  setup             Initial project setup"
    echo "  release           Create a new release (requires version)"
    echo "  ci-local          Simulate CI pipeline locally"
    echo "  env-check         Check development environment"
    echo "  --version         Show version information"
    echo ""
    echo "Examples:"
    echo "  $0 test           # Run unit tests"
    echo "  $0 build          # Build debug APK"
    echo "  $0 release v1.0.0 # Create release v1.0.0"
    echo "  $0 env-check      # Verify environment setup"
}

# Function to check environment
check_environment() {
    print_header "Environment Check"
    
    # Check Java
    print_info "Checking Java version..."
    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | head -n 1)
        print_success "Java found: $java_version"
    else
        print_error "Java not found. Please install JDK 17 or later."
    fi
    
    # Check Android SDK
    print_info "Checking Android SDK..."
    if detect_android_sdk; then
        print_success "Android SDK configured correctly"
    else
        print_error "Android SDK not found or not configured"
    fi
    
    # Check Git
    print_info "Checking Git..."
    if command -v git &> /dev/null; then
        git_version=$(git --version)
        print_success "Git found: $git_version"
    else
        print_error "Git not found. Please install Git."
    fi
    
    # Check Gradle wrapper
    print_info "Checking Gradle wrapper..."
    if [ -f "gradlew" ]; then
        if [ -x "gradlew" ]; then
            print_success "Gradle wrapper found and executable"
        else
            print_warning "Gradle wrapper found but not executable. Run: chmod +x gradlew"
        fi
    else
        print_error "Gradle wrapper not found. Are you in the android directory?"
    fi
    
    print_info "Environment check completed"
}

# Function to show version
show_version() {
    echo "Homeal Android Development Helper v1.0.0"
    echo "Project: Homeal Android App"
    echo "Created: August 2024"
    echo ""
    echo "System Information:"
    echo "  OS: $(uname -s)"
    echo "  Architecture: $(uname -m)"
    echo "  Script location: $SCRIPT_DIR"
    echo ""
    if command -v java &> /dev/null; then
        echo "Java: $(java -version 2>&1 | head -n 1)"
    fi
    if [ -n "$ANDROID_HOME" ]; then
        echo "Android SDK: $ANDROID_HOME"
    fi
}

# Function to run unit tests
run_tests() {
    print_header "Running Unit Tests"
    ./gradlew test
    print_success "Unit tests completed"
}

# Function to run all tests
run_all_tests() {
    print_header "Running All Tests"
    print_info "Running unit tests..."
    ./gradlew test
    print_info "Running instrumented tests..."
    ./gradlew connectedAndroidTest
    print_success "All tests completed"
}

# Function to build debug APK
build_debug() {
    print_header "Building Debug APK"
    ./gradlew assembleDebug
    print_success "Debug APK built successfully"
    print_info "APK location: app/build/outputs/apk/debug/app-debug.apk"
}

# Function to build release APK
build_release() {
    print_header "Building Release APK"
    ./gradlew assembleRelease
    print_success "Release APK built successfully"
    print_info "APK location: app/build/outputs/apk/release/app-release-unsigned.apk"
}

# Function to run lint
run_lint() {
    print_header "Running Lint Checks"
    ./gradlew lint
    print_success "Lint checks completed"
    print_info "Lint report: app/build/reports/lint-results-debug.html"
}

# Function to clean project
clean_project() {
    print_header "Cleaning Project"
    ./gradlew clean
    print_success "Project cleaned"
}

# Function to detect Android SDK
detect_android_sdk() {
    print_info "Detecting Android SDK location..."
    
    # Check if ANDROID_HOME is already set
    if [ -n "$ANDROID_HOME" ] && [ -d "$ANDROID_HOME" ]; then
        print_success "Android SDK found at: $ANDROID_HOME"
        return 0
    fi
    
    # Common Android SDK locations
    local sdk_paths=(
        "$HOME/Android/Sdk"
        "$HOME/Library/Android/sdk"
        "$HOME/android-sdk"
        "/opt/android-sdk"
        "/usr/local/android-sdk"
        "$LOCALAPPDATA/Android/Sdk"  # Windows
    )
    
    for path in "${sdk_paths[@]}"; do
        if [ -d "$path" ]; then
            export ANDROID_HOME="$path"
            print_success "Android SDK detected at: $ANDROID_HOME"
            echo "sdk.dir=$ANDROID_HOME" > local.properties
            print_info "Created local.properties with SDK path"
            return 0
        fi
    done
    
    print_warning "Android SDK not found automatically"
    print_info "Please set ANDROID_HOME environment variable or install Android Studio"
    print_info "Common locations:"
    print_info "  Linux:   ~/Android/Sdk"
    print_info "  macOS:   ~/Library/Android/sdk"
    print_info "  Windows: %LOCALAPPDATA%\\Android\\Sdk"
    return 1
}

# Function to setup project
setup_project() {
    print_header "Setting Up Project"
    
    # Detect Android SDK
    if ! detect_android_sdk; then
        print_error "Cannot proceed without Android SDK. Please install Android Studio or set ANDROID_HOME."
        exit 1
    fi
    
    print_info "Making gradlew executable..."
    chmod +x gradlew
    print_info "Downloading dependencies..."
    ./gradlew dependencies
    print_success "Project setup completed"
}

# Function to create release
create_release() {
    local version=$1
    if [ -z "$version" ]; then
        print_error "Version required for release"
        echo "Usage: $0 release <version>"
        echo "Example: $0 release v1.0.0"
        exit 1
    fi

    print_header "Creating Release $version"
    
    # Validate version format
    if [[ ! $version =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        print_error "Invalid version format. Use: v<major>.<minor>.<patch> (e.g., v1.0.0)"
        exit 1
    fi

    # Check if we're on main branch
    current_branch=$(git branch --show-current)
    if [ "$current_branch" != "main" ]; then
        print_warning "You're not on the main branch (current: $current_branch)"
        read -p "Continue anyway? [y/N] " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_info "Release cancelled"
            exit 0
        fi
    fi

    # Check for uncommitted changes
    if ! git diff-index --quiet HEAD --; then
        print_error "You have uncommitted changes. Please commit or stash them first."
        exit 1
    fi

    # Run tests before release
    print_info "Running tests before release..."
    run_tests

    # Create and push tag
    print_info "Creating git tag $version..."
    git tag "$version"
    git push origin "$version"
    
    print_success "Release $version created and pushed to GitHub"
    print_info "GitHub Actions will automatically build and publish the release"
    print_info "Check: https://github.com/$(git config --get remote.origin.url | sed 's/.*github.com[:/]\([^/]*\/[^/]*\).*/\1/' | sed 's/\.git$//')/actions"
}

# Function to simulate CI locally
simulate_ci() {
    print_header "Simulating CI Pipeline Locally"
    
    print_info "Step 1: Running unit tests..."
    run_tests
    
    print_info "Step 2: Running lint checks..."
    run_lint
    
    print_info "Step 3: Building debug APK..."
    build_debug
    
    print_success "CI simulation completed successfully"
}

# Main script logic
case "$1" in
    "test")
        run_tests
        ;;
    "test-all")
        run_all_tests
        ;;
    "build")
        build_debug
        ;;
    "build-release")
        build_release
        ;;
    "lint")
        run_lint
        ;;
    "clean")
        clean_project
        ;;
    "setup")
        setup_project
        ;;
    "release")
        create_release "$2"
        ;;
    "ci-local")
        simulate_ci
        ;;
    "env-check")
        check_environment
        ;;
    "--version" | "-v")
        show_version
        ;;
    *)
        show_usage
        exit 1
        ;;
esac
