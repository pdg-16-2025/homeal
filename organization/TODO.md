
## Required Actions to Finalize

### Priority 1 - Android Environment
1. **Create Android Studio project**
   ```bash
   # Create new Android project
   # - Template: Empty Compose Activity
   # - Language: Kotlin
   # - Minimum SDK: API 21
   ```

2. **Configure build.gradle** with required dependencies

3. **Create package structure** according to MVC architecture

### Priority 2 - Issue Tracking
1. **Configure GitHub Issues**
   ```yaml
   # Templates to create
   - .github/ISSUE_TEMPLATE/bug_report.md
   - .github/ISSUE_TEMPLATE/feature_request.md
   - .github/ISSUE_TEMPLATE/user_story.md
   ```

2. **Create standardized labels**
   ```
   - type: bug
   - type: feature  
   - type: documentation
   - priority: high/medium/low
   - component: ui/database/scanner/etc.
   ```

### Priority 3 - CI/CD Setup
**Status**: ❌ **NOT IMPLEMENTED** - Requires Android project first

1. **GitHub Actions workflow** (to be created after Android setup)
   ```yaml
   # .github/workflows/android.yml - TEMPLATE READY
   name: Android CI
   on: [push, pull_request]
   jobs:
     test:
       runs-on: ubuntu-latest
       steps:
         - uses: actions/checkout@v3
         - name: Set up JDK 11
           uses: actions/setup-java@v3
         - name: Run tests
           run: ./gradlew test
   ```

2. **Current CI/CD Status**:
   - ❌ Workflow files not created
   - ❌ Automated testing not configured
   - ❌ Build automation not set up
   - ⚠️ **Blocked by**: Missing Android Studio project

## Kilian

- Add reviews to the DB
- Find the different quantities