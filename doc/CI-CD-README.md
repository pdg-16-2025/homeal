# CI/CD Setup for Homeal Android App

## Overview
This document describes the complete CI/CD pipeline setup for the Homeal Android application using GitHub Actions.

## Features

### Automated Testing
- **Unit Tests**: Run on every push to main branch and PR
- **Lint Checks**: Automated code quality analysis
- **Test Reports**: Detailed test results with pass/fail status
- **Pull Request Validation**: Automatic checks on PRs with status comments

### Automated Building
- **Debug APK**: Built on every successful test run
- **Release APK**: Built automatically on version tags
- **Build Artifacts**: APKs uploaded and available for download
- **Build Caching**: Gradle dependencies cached for faster builds

### Release Management
- **Tag-based Releases**: Create releases by pushing version tags (e.g., `v1.0.0`)
- **Automatic Versioning**: Version code and name updated automatically
- **GitHub Releases**: Automatic creation with APK attachments
- **Semantic Versioning**: Following `v<major>.<minor>.<patch>` format

## File Structure

```
.github/workflows/
├── android-ci.yml       # Main CI pipeline (tests, build, lint)
└── android-release.yml  # Release pipeline (triggered by tags)

scripts/
└── android-dev.sh      # Development helper script

android/
├── build.gradle.kts    # Enhanced with test dependencies
├── app/build.gradle.kts # Updated with test configuration
└── gradle/libs.versions.toml # Updated with testing libraries
```

## Workflows

### 1. Android CI (`android-ci.yml`)
**Triggers**: Push to `main` or `develop`, PRs to `main`

**Jobs**:
- `test`: Run unit tests with detailed reporting
- `build`: Build debug APK after successful tests
- `lint`: Run Android lint checks

### 2. Android Release (`android-release.yml`)
**Triggers**: Push tags matching `v*` pattern

**Jobs**:
- `release`: Full release pipeline including:
  - Test execution
  - Version number updates
  - Release and debug APK builds
  - GitHub release creation
  - APK attachment to release



## 🛠 Development Tools

### Enhanced Testing Setup
- **Mockito**: For mocking dependencies
- **Truth**: Google's fluent assertion library
- **Coroutines Test**: For testing Kotlin coroutines
- **Architecture Testing**: For testing Android Architecture Components

### Development Script (`scripts/android-dev.sh`)
Quick commands for common tasks:

```bash
# Run tests
./scripts/android-dev.sh test

# Build debug APK
./scripts/android-dev.sh build

# Create a release
./scripts/android-dev.sh release v1.0.0

# Simulate CI locally
./scripts/android-dev.sh ci-local

# Clean project
./scripts/android-dev.sh clean
```

## Getting Started

### 1. Initial Setup
```bash
# Make development script executable
chmod +x scripts/android-dev.sh

# Setup project dependencies
./scripts/android-dev.sh setup
```

### 2. Running Tests Locally
```bash
# Run unit tests
./scripts/android-dev.sh test

# Run all tests (requires connected device/emulator)
./scripts/android-dev.sh test-all
```

### 3. Creating Your First Release
```bash
# Ensure you're on main branch with latest changes
git checkout main
git pull

# Create and push a release tag
git tag v1.0.0
git push origin v1.0.0
```

The release workflow will automatically:
1. Run all tests
2. Build release APKs
3. Create a GitHub release
4. Attach APK files to the release

## Best Practices

### Branching Strategy
- `main`: Production-ready code
- `develop`: Integration branch for features
- Feature branches: `feature/description`
- Hotfix branches: `hotfix/description`

### Testing Guidelines
- Write unit tests for all business logic
- Aim for >80% code coverage
- Use descriptive test names
- Test both happy path and edge cases

### Release Process
1. Develop features on feature branches
2. Create PR to `develop` branch
3. Merge to `develop` for integration testing
4. Create PR from `develop` to `main`
5. After merge to `main`, create release tag

### Version Numbering
- Follow semantic versioning: `v<major>.<minor>.<patch>`
- Increment major for breaking changes
- Increment minor for new features
- Increment patch for bug fixes

## Monitoring and Debugging

### GitHub Actions
- View workflow runs in the "Actions" tab
- Check logs for failed builds
- Download build artifacts from successful runs

### Local Testing
```bash
# Simulate the CI pipeline locally
./scripts/android-dev.sh ci-local

# Check specific issues
./scripts/android-dev.sh lint
```

### Common Issues
1. **Test Failures**: Check test reports in workflow logs
2. **Build Failures**: Verify Gradle configuration
3. **Lint Issues**: Run lint locally and fix reported issues
4. **Release Failures**: Ensure proper tag format and permissions

## Next Steps

### Enhancements to Consider
1. **Code Coverage**: Add Jacoco for coverage reports
2. **Static Analysis**: Add additional static analysis tools
3. **Security**: Add security scanning for dependencies
4. **Performance**: Add performance testing automation
5. **App Signing**: Set up proper app signing for release builds
6. **Play Store**: Automate Play Store deployment

### Integration with Server
When you add the server component:
1. Create separate workflows for server CI/CD
2. Add integration tests between Android app and server
3. Consider using Docker for server deployment
4. Add database migration testing

## Contributing
1. Create feature branch from `develop`
2. Write tests for new functionality
3. Ensure all tests pass locally
4. Create PR with descriptive title and description
5. Wait for automated checks to pass
6. Request code review

---

This CI/CD setup provides a solid foundation for Android development with automated testing, building, and releasing. The pipeline ensures code quality and makes the release process smooth and reliable.
