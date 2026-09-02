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

- Kotlin: 2.4.0 (required by the SwiftPM import used on Apple targets)
- Coroutines: 1.10.2
- Serialization: 1.9.0
- Firebase BOM: 34.17.0
- firebase-ios-sdk: 12.17.0 (minimum deployment targets iOS 15 / tvOS 15 / macOS 10.15)
- Java target: 17
- Android minSdk: 23, compileSdk: 34

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
- JS target uses Firebase Web SDK.
- Binary compatibility is validated on every PR via the kotlinx binary compatibility validator — breaking changes require a major version bump.

### Mirrored source sets — change together

Some platform code is hand-mirrored with **no compiler enforcement that the copies stay in sync**. A change to one side must be made to the other, in the same commit.

- **`jsMain` ↔ `wasmJsMain`** (~3,900 / ~4,200 lines, all modules with a JS target) and **`jsTest` ↔ `wasmJsTest`**. Every JS change has to be made twice. `test-utils/src/wasmJsMain` additionally re-implements JS-value conversion a third time, because it cannot depend on `firebase-common`.
- **`androidMain` ↔ `jvmMain`**, but **only** in `firebase-auth`, `firebase-analytics`, `firebase-crashlytics` and `firebase-messaging`. The other 10 modules share source physically via `kotlin.srcDir("src/androidMain/kotlin")` in their `jvmMain` source set, so drift there is impossible and no manual sync is needed.

`androidUnitTest` and `jvmTest` are **not** mirrors — those files are per-platform `actual` test config (test context, `@Ignore` aliasing) and are expected to differ.

Divergence between mirrors is sometimes deliberate — a platform genuinely lacking an API — so a difference is not automatically a bug. But an *unexplained* one usually is.

`firebase-auth` is forked rather than shared because the GitLive Firebase Java SDK lacks four APIs that `androidMain` uses: `PhoneAuthOptions`, `AuthResult.getCredential()`, `AuthResult.getAdditionalUserInfo()`/`AdditionalUserInfo`, and `ActionCodeSettings.Builder.setLinkDomain()`. Tracked in GitLiveApp/firebase-java-sdk#68; when that lands the fork can be deleted and `srcDir` sharing restored.
