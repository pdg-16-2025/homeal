# Work Process Description

## Overview

The Homeal project work process is organized around **short one-week sprints** with a hybrid methodology combining on-site and remote work. The organization prioritizes direct communication and agile coordination.

## Work Methodology

### Sprint Management
- **Sprint Duration**: 1 week
- **Rhythm**: Short cycles for rapid feedback and adaptation
- **Planning**: Weekly planning and review

### Weekly Organization

#### On-Site Days: Monday and Thursday
- **Full work sessions** with direct team communication
- **Sprint planning** and backlog refinement
- **Product discussions** and decision making
- **Code reviews** and pair programming sessions
- **Real-time blocker resolution**

#### Remote Days: Tuesday, Wednesday, Friday
- **Discord communication** for progress assessment
- **Task distribution and coordination**
- **Daily standups** via Discord
- **Individual work** on assigned tasks

### Communication Channels
- **On-site**: Face-to-face collaboration and planning
- **Remote**: Discord for daily check-ins and task updates

## Git Flow and Version Management

### Branching Strategy

#### Branch Structure
- **Main branch**: `main` - Production-ready code
- **Development branches**: `dev/issue-number-description`

#### Branch Management
- **One branch per issue/specification**
- **Feature isolation** for parallel development
- **Organized division** by folder/code according to specification domain
- **Each team member** works on their assigned specification branch

### Git Workflow

#### Feature Development
1. **Branch creation** from `main`
   ```bash
   git checkout main
   git pull origin main
   git checkout -b dev/123-fridge-management
   ```

2. **Development and commits**
   ```bash
   git add .
   git commit -m "feat(fridge): add ingredient scanning functionality"
   git push origin feature/123-fridge-management
   ```

3. **Pull Request and Review**
   - Create PR to `main`
   - Mandatory code review
   - Passing automated tests
   - Functional validation

4. **Merge and cleanup**
   ```bash
   git checkout main
   git pull origin main
   git branch -d dev/123-fridge-management
   ```

### Naming Conventions

#### Commit Messages
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types**:
- `feat`: new feature
- `fix`: bug fix
- `docs`: documentation
- `style`: formatting, no logic change
- `refactor`: code refactoring
- `test`: adding/modifying tests
- `chore`: maintenance tasks

**Examples**:
```
feat(scan): implement barcode scanning functionality
fix(database): resolve SQLite connection leak
docs(api): update controller documentation
```

## Quality Assurance Process

### Code Review
- **Schedule**: Every Monday
- **Scope**: Functionality verification and code quality
- **Requirements**:
  - Feature completeness validation
  - Code style adherence

### Quality Gates
- All tests must pass before merge
- Code review approval required (1 collaborator)
- Documentation updates mandatory

### Testing Strategy
- **Unit tests**: Automated testing of core functionality
- **Integration tests**: Component interaction validation
- **UI tests**: User interface and user experience testing
- **Manual testing**: Feature verification and edge cases

## Project Management

### Work Breakdown
- **Division by specification**: Each member responsible for a domain
- **GitHub Issues**: Granular task tracking
- **Milestones**: Clearly defined sprint objectives
- **Labels**: Issue categorization (bug, feature, documentation, etc.)

### Progress Tracking
- **Daily standups** (Discord):
  - What was done yesterday
  - What will be done today
  - Identified blockers

- **Sprint reviews** (on-site):
  - Demonstration of developed features
  - Feedback and adjustments
  - Next sprint planning

## Tools and Environment

### Development Tools
- **IDE**: Android Studio (latest stable version)
- **Version control**: Git with GitHub
- **Communication**: Discord for remote work
- **Project management**: GitHub Issues and Projects

### Development Environment
```bash
# Minimum required configuration
- Android Studio (latest version)
- Git
- Kotlin plugin
- Android SDK (API level as per project requirements)
```