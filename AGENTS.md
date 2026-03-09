# AGENTS.md - Developer and AI Agent Guide

This document provides essential information for developers and AI agents working on this Minecraft mod's codebase.

## Project Overview

This is a Minecraft mod. See README.md for a description of what this mod does. The project is written in Java and uses Gradle as its build system.

## Multi-Loader Architecture

**This repo only uses a multi-loader architecture if you can find a `loader-common` directory in the root of this repo!**

In Minecraft 1.21 and above, this repository uses a **multi-loader setup**, meaning the mod is available on multiple Minecraft mod loaders.
The Minecraft version can be found in `gradle.properties`.
Understanding this architecture is crucial when making changes:

### Directory Structure

- **`loader-common/`**: Contains source code that is common across all mod loaders. Most shared functionality should be implemented here.
- **`loader-fabric/`**: Fabric-specific implementation and integration code.
- **`loader-forge/`**: Forge-specific implementation and integration code (for older Minecraft versions).
- **`loader-neoforge/`**: NeoForge-specific implementation and integration code (for newer Minecraft versions).

### Making Changes in Multi-loader Setups

When adding features or fixing bugs:
1. Place shared logic in `loader-common/` whenever possible
2. Only add loader-specific code to the respective `loader-*` directories when platform-specific APIs are required
3. Ensure your changes work across all supported loaders

## Testing

This mod uses two types of tests:

### 1. Unit Tests

**Location**: `src/test/java/`

Traditional JUnit tests for testing isolated functionality without requiring a full Minecraft instance.

**Running unit tests**:
```bash
./gradlew test
```

Unit tests are automatically executed when running the `build` command.

### 2. Game Tests

**Location**: Within normal sources, typically in the `org/cyclops/*/gametest` package (e.g., `loader-common/src/main/java/org/cyclops/cyclopscore/gametest/`)

Game tests run an actual Minecraft instance to test code with real game logic. These are essential for testing features that interact with Minecraft's gameplay systems.
Game tests only exist in Minecraft 1.21 and higher.

**Running game tests**:
```bash
./gradlew runGameTestServer
```

**Important**:
- Game tests are **NOT** run automatically during the build process
- For Minecraft 1.21 and above, game tests must be run manually before committing
- Game tests must pass before finalizing your changes

### When to Add Tests

When adding new features or fixing bugs:
- **Always** add unit tests when possible for isolated logic
- **Always** add game tests when the feature interacts with Minecraft gameplay systems
- Look at existing tests in the respective directories for examples of test patterns and conventions

### Testing advancements

When writing game tests for advancements, don't just call criterion triggers directly, but try to simulate actual game logic to invoke the criterion triggers indirectly.

### Test coverage

When adding new tests for the sake of increasing test coverage, you can measure coverage by running `./gradlew test runGameTestServer jacocoTestReport`, and checking the coverage output in `build/reports/jacoco/test/`.

## Building the Project

### Prerequisites

- Java version is specified in `gradle.properties` (otherwise, default to version 17)
- Gradle (use the provided wrapper: `./gradlew`)

### Build Command

Before every commit, ensure the project builds successfully:

```bash
./gradlew build
```

This command will:
- Compile all source code
- Run unit tests automatically
- Generate build artifacts

### Full Pre-Commit Validation

Run build:

```bash
./gradlew build
```

Only for Minecraft 1.21 and above, also run game tests:
```bash
./gradlew runGameTestServer
```

Both must pass before committing changes.

## Code Formatting

This project uses Spotless for code formatting:

```bash
./gradlew spotlessApply
```

The pre-commit script in `scripts/pre-commit` automatically formats staged files. Consider setting it up as a Git hook:

```bash
ln -s ../../scripts/pre-commit .git/hooks/pre-commit
```

## Development Workflow

1. **Understand the change**: Read the issue/feature request thoroughly
2. **Explore the codebase**: Use tools like `grep` to find relevant code
3. **Make minimal changes**: Focus on the specific issue/feature
4. **Add tests**: Write unit tests and/or game tests as appropriate
5. **Build and test**:
   ```bash
   ./gradlew build
   ./gradlew runGameTestServer  # For MC 1.21+
   ```
6. **Format code**: `./gradlew spotlessApply`
7. **Commit**: Use clear, descriptive commit messages

## Release Management

Version bumping and release management helper scripts are available in the [CyclopsMC/ReleaseHelpers](https://github.com/CyclopsMC/ReleaseHelpers) repository. These bash scripts assist with:
- Version bumping across all loaders
- Changelog management
- Release preparation

## Gradle Tasks Reference

Common Gradle tasks for development:

| Task | Purpose |
|------|---------|
| `./gradlew build` | Build the project and run unit tests |
| `./gradlew test` | Run unit tests only |
| `./gradlew runGameTestServer` | Run game tests (manual, required for MC 1.21+) |
| `./gradlew spotlessApply` | Format code according to project standards |
| `./gradlew publishToMavenLocal` | Publish to local Maven for testing in other projects |

If for any reason gradle fails due to internet connection issues, try running offline instead by running the gradle command with the `--offline` flag.

## CI/CD

GitHub Actions automatically:
- Builds the project on every push and pull request
- Runs unit tests
- Runs game tests (including `runGameTestServer`)
- Generates coverage reports
- Deploys to CurseForge, Modrinth, and Maven on appropriate branches/tags

See `.github/workflows/ci.yml` for the full CI configuration.

## Additional Resources

- **README.md**: Project overview and usage information
- **CONTRIBUTING.md**: Contribution guidelines and issue reporting
- **Build configuration**: `build.gradle` and loader-specific build files
- **Project properties**: `gradle.properties` (Minecraft version, mod version, etc.)

## Key Principles

1. **Minimal changes**: Make the smallest possible changes to achieve your goal
2. **Test coverage**: Always try to add tests for new features and bug fixes (may be difficult or impossible for things related to GUIs)
3. **Multi-loader compatibility**: Ensure changes work across all supported loaders
4. **Build validation**: Never commit without running `build` (and `runGameTestServer` for MC 1.21+)
5. **Code quality**: Follow existing patterns and conventions in the codebase

## Code changes across Minecraft updates

When I ask you to things such as "fix upmerge issues" or "update to the next Minecraft version",
this means that I want you to help resolve compilation errors
that were introduced due to upmerging code from an older minecraft version to a newer minecraft version.
The old minecraft version can be found in the `.upmerge-src-branch` file of the parent directory,
and the new minecraft version can be found in `gradle.properties`.
Changes may also include fixing merge conflicts.

IMPORTANT: Some changes may be non-trivial,
for which you can find detail background information for each seperate Minecraft update within these primers: https://github.com/neoforged/.github/tree/main/primers
The blogs of https://neoforged.net/ and https://fabricmc.net/blog/ may also contain useful porting help.

After making all necessary changes, make sure the code fully compiles and the (game) tests pass.
