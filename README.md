# Smart Expense Manager

A modern Android expense-tracking application built with Kotlin. This repository contains the `app` Android module and project-level Gradle configuration used for building, testing, and releasing the application.

## Table of Contents
- **Project Overview**: high-level purpose and features
- **Technology Stack**: languages and key libraries
- **Repository Layout**: important files and directories
- **Requirements**: local dev prerequisites
- **Getting Started**: clone, build, run
- **Testing**: unit and instrumentation tests
- **Code Style & Architecture**: conventions and patterns
- **Contributing**: workflow and PR guidance
- **License & Contact**n+
## Project Overview

Smart Expense Manager is a compact, production-oriented Android application designed to help users record and monitor expenses. The app includes features such as transaction recording, categorization, Mpesa (mobile money) parsing/notifications, onboarding, and settings management.

## Technology Stack

- Language: Kotlin
- Build system: Gradle (Gradle Kotlin DSL)
- Android: Android SDK (Jetpack libraries expected: ViewModel, LiveData/Flow, Room, Navigation)
- Testing: JUnit for unit tests, Android instrumentation tests for UI/integration

## Repository Layout

- `app/` — Android application module containing source, resources and Gradle configuration.
  - `app/src/main/java/com/example/smartexpensemanager/` — primary Kotlin packages:
    - `data/` — local, remote, repository code and DAO implementations
    - `service/` — background services (e.g., `MpesaNotificationListenerService.kt`)
    - `ui/` — UI screens and feature folders (`auth`, `dashboard`, `transaction`, etc.)
    - `util/` — app utility helpers (formatters, Activity ext, navigation helpers)
  - `app/src/androidTest/` — instrumentation tests
  - `app/src/main/res/` — XML layouts, drawables, values, and theming resources
- `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradle/` — project-level build and Gradle wrapper

Use this README as the canonical reference for building and working with the app.

## Requirements

- JDK 11 or newer installed and available in `PATH`.
- Android SDK with appropriate API levels installed (recommended: API 31+).
- Android Studio (recommended) or command-line Gradle via the included wrapper.
- Recommended OS-specific notes:
  - Windows: use `gradlew.bat` (provided)
  - macOS / Linux: use `./gradlew`

## Getting Started (Developer)

1. Clone the repository:

```bash
git clone <repository-url>
cd <repo-root>
```

2. Build using the Gradle wrapper:

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

On macOS/Linux:

```bash
./gradlew assembleDebug
```

3. Run in Android Studio:

- Open the project root in Android Studio.
- Let Studio import Gradle and sync dependencies.
- Use the Run/Debug configuration to install on an emulator or device.

4. Install built APK via ADB (optional):

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Testing

- Run JVM unit tests:

```bash
./gradlew test
```

- Run instrumentation tests on a connected device/emulator:

```bash
./gradlew connectedAndroidTest
```

- Run lint checks:

```bash
./gradlew lint
```

## Code Style & Architecture

- Kotlin idioms should be followed (explicit types for public APIs, avoid unnecessary `!!`).
- Use coroutines and structured concurrency for asynchronous work.
- Repository pattern is used to separate data sources (`data/local`, `data/remote`) from UI logic.
- UI components should be lifecycle-aware and use `ViewModel` for state.
- Keep resources organized under `res/` and follow Material Design and theming conventions.

## Important Files to Know

- [app/build.gradle.kts](app/build.gradle.kts#L1) — `app` module Gradle configuration
- [settings.gradle.kts](settings.gradle.kts#L1) — project settings
- [app/src/main/java/com/example/smartexpensemanager/SmartExpenseApp.kt](app/src/main/java/com/example/smartexpensemanager/SmartExpenseApp.kt#L1) — application entrypoint

## Contributing

1. Fork the repository and create a feature branch: `feature/brief-description`.
2. Keep changes small and focused; open PRs against the `main` branch.
3. Include tests for new features or bug fixes when applicable.
4. Ensure lint and unit tests pass locally before requesting review.

## Release & CI suggestions

- Add a CI pipeline to run `./gradlew assembleDebug`, `./gradlew test`, and `./gradlew lint` on every PR.
- Consider using semantic versioning and tags for releases.

## Troubleshooting

- If Gradle sync fails, run `./gradlew --refresh-dependencies`.
- If SDK manifest or compile errors occur, ensure the Android SDK and build-tools versions align with the project settings in `app/build.gradle.kts`.

## License & Contact

Specify a license for the project (e.g., MIT, Apache-2.0) in a `LICENSE` file. For questions or collaborations, open an issue or contact the maintainers via the repository.

