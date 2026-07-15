# Firebase Kotlin SDK - Claude Instructions

## Project Overview

Kotlin-first multiplatform Firebase SDK maintained by GitLive. Wraps official Firebase SDKs for Android, iOS, JVM, and JS into a single idiomatic Kotlin API.

## Module Structure

- `firebase-app`, `firebase-auth`, `firebase-firestore`, `firebase-database`, `firebase-functions`, `firebase-analytics`, `firebase-storage`, `firebase-messaging`, `firebase-config`, `firebase-crashlytics`, `firebase-installations`, `firebase-perf` — Firebase service modules
- `firebase-common` / `firebase-common-internal` — Shared utilities
- `test-utils` — Shared test helpers
- `buildSrc` — Gradle build conventions

## Build & Test Commands

```bash
# Lint / format
./gradlew lintKotlin
./gradlew formatKotlin

# API compatibility
./gradlew apiDump         # update .api files after public API changes
./gradlew apiCheck        # validate no breaking changes

# Tests by platform
./gradlew jvmTest
./gradlew jsNodeTest
./gradlew iosSimulatorArm64Test
./gradlew macosArm64Test
./gradlew connectedAndroidTest   # requires emulator

# Publish locally
./gradlew publishToMavenLocal
```

## Key Versions (gradle/libs.versions.toml)

- Kotlin: 2.2.20
- Coroutines: 1.10.2
- Serialization: 1.9.0
- Firebase BOM: 33.15.0
- Java target: 17
- Android minSdk: 21, compileSdk: 34

## KMP Targets

Android, iOS (arm64, x64, simulatorArm64), macOS (arm64, x64), tvOS (arm64, x64, simulatorArm64), JVM, JS (IR, CommonJS).

## Coding Conventions

- **Style:** IntelliJ Kotlin code style enforced by Kotlinter (ktlint). Run `./gradlew formatKotlin` before committing.
- **API design — Kotlin-first:**
  - `suspend fun` instead of callbacks/Tasks
  - `Flow` instead of listeners
  - Default arguments instead of builder pattern
  - `@Serializable` data classes for Firestore/Database models
- **Visibility:** `explicitApi()` is enforced — all public declarations need explicit visibility modifiers.
- **Expect/actual:** Public APIs declared as `expect` in `commonMain`; platform implementations in `androidMain`, `iosMain`, `jsMain`, `jvmMain`.
- **After any public API change:** run `./gradlew apiDump` to update `.api` files and commit them alongside the change.
- **KDoc** required on all public APIs.

## Important Notes

- iOS/macOS/tvOS consume the Firebase iOS SDK via Kotlin's Swift Package Manager integration (`swiftPMDependencies`, Kotlin 2.4+); Firebase iOS frameworks must be linked separately by consumers (not transitive). See `documentation/ios-firebase-linking.md`.
- JVM target reuses `androidMain` source set code via GitLive Firebase Java SDK.
- JS target uses Firebase Web SDK.
- Binary compatibility is validated on every PR via the kotlinx binary compatibility validator — breaking changes require a major version bump.
