# Firebase Kotlin SDK - Claude Instructions

## Project Overview

Kotlin-first multiplatform Firebase SDK maintained by GitLive. Wraps official Firebase SDKs for Android, iOS, JVM, JS and Wasm (wasmJs) into a single idiomatic Kotlin API.

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
./gradlew :<module>:androidSourceCompatDump   # modules with api/android-sdk/ (firebase-app, firebase-installations): regenerate api/android-sdk-compat.txt, checked by `check`

# Tests by platform
./gradlew jvmTest
./gradlew jsNodeTest
./gradlew wasmJsNodeTest
./gradlew iosSimulatorArm64Test
./gradlew macosArm64Test
./gradlew connectedAndroidTest   # requires emulator

# Publish locally
./gradlew publishToMavenLocal
```

## Key Versions (gradle/libs.versions.toml)

- Kotlin: 2.4.20 (2.4+ is required by the SwiftPM import used on Apple targets)
- Coroutines: 1.10.2
- Serialization: 1.9.0
- Firebase BOM: 34.18.0
- firebase-ios-sdk: 12.17.0 (minimum deployment targets iOS 15 / tvOS 15 / macOS 12; Firebase itself allows macOS 10.15, but Kotlin/Native builds macOS code for 12)
- Java target: 17
- Android minSdk: 23, compileSdk: 34

## KMP Targets

Android, iOS (arm64, x64, simulatorArm64), macOS (arm64), tvOS (arm64, simulatorArm64), JVM, JS (IR, CommonJS), Wasm (wasmJs). `macosX64` and `tvosX64` were dropped in 3.0.0: Kotlin deprecated them in 2.3.20, makes them errors in 2.5.0 (KT-84955) and removes them in 2.5.20. `iosX64` is not deprecated and stays.

## Coding Conventions

- **Style:** IntelliJ Kotlin code style enforced by Kotlinter (ktlint). Run `./gradlew formatKotlin` before committing.
- **API design — Kotlin-first:**
  - `suspend fun` instead of callbacks/Tasks
  - `Flow` instead of listeners
  - Default arguments instead of builder pattern
  - `@Serializable` data classes for Firestore/Database models
  - These apply to the `dev.gitlive.firebase` API. The `com.google.firebase.*` source-compatibility layer (firebase-app, firebase-installations) mirrors the Firebase Android SDK exactly, `Task` and listener interfaces included; see "API Compatibility Goal" in `.github/copilot-instructions.md`.
- **Visibility:** `explicitApi()` is enforced — all public declarations need explicit visibility modifiers.
- **Expect/actual:** Public APIs declared as `expect` in `commonMain`; platform implementations in `androidMain`, `appleMain`, `jsMain`, `wasmJsMain`, `jvmMain`. Modules using `utils.applyFirebaseHierarchy()` (firebase-app, firebase-installations) also have `nonJsMain` (android, jvm, apple) and `nonJvmMain` (js, wasmJs, apple).
- **After any public API change:** run `./gradlew apiDump` to update `.api` files (and `androidSourceCompatDump` for firebase-app / firebase-installations) and commit them alongside the change.
- **KDoc** required on all public APIs.

## Important Notes

- iOS/macOS/tvOS consume the Firebase iOS SDK via Kotlin's Swift Package Manager integration (`swiftPMDependencies`, Kotlin 2.4+). Firebase is not bundled in the klibs; each module publishes its `firebase-ios-sdk` SwiftPM dependency as Maven metadata, which consumers inherit transitively. Because of that metadata, the Kotlin Gradle plugin fails consumers on the Kotlin CocoaPods plugin, or on direct integration without the generated linkage package in Xcode, until they migrate to SwiftPM integration. See `documentation/ios-firebase-linking.md`.
- After changing `firebase-ios-sdk` in `gradle/libs.versions.toml`, run `rm -rf build/kotlin/swiftPMXcodeDumps` before building on macOS. Kotlin 2.4.20 keeps the SwiftPM xcodebuild state in shared folders under the root `build/kotlin/` and can reuse it after a version change, failing with "module.modulemap has been modified" (KT-88106, fix planned for Kotlin 2.5.0).
- JS target uses Firebase Web SDK.
- Binary compatibility is validated on every PR via the kotlinx binary compatibility validator — breaking changes require a major version bump.

### Mirrored source sets — change together

Some platform code is hand-mirrored with **no compiler enforcement that the copies stay in sync**. A change to one side must be made to the other, in the same commit.

- **`jsMain` ↔ `wasmJsMain`** (~3,900 / ~4,200 lines, all modules with a JS target) and **`jsTest` ↔ `wasmJsTest`**. Every JS change has to be made twice. `test-utils/src/wasmJsMain` additionally re-implements JS-value conversion a third time, because it cannot depend on `firebase-common`. The `com.google.firebase` actuals in `jsMain` (`*.js.kt`) are mirrored as `*.wasmJs.kt` in `wasmJsMain`; `nonJvmMain` is source compiled for js, wasmJs and apple alike, so it is shared rather than mirrored.
- **`androidMain` ↔ `jvmMain`**, but **only** in `firebase-auth`, `firebase-analytics`, `firebase-crashlytics` and `firebase-messaging`. The other 10 modules share source physically via `kotlin.srcDir("src/androidMain/kotlin")` in their `jvmMain` source set, so drift there is impossible and no manual sync is needed.

`androidUnitTest` and `jvmTest` are **not** mirrors — those files are per-platform `actual` test config (test context, `@Ignore` aliasing) and are expected to differ.

Divergence between mirrors is sometimes deliberate — a platform genuinely lacking an API — so a difference is not automatically a bug. But an *unexplained* one usually is.

`firebase-auth` is forked rather than shared because the GitLive Firebase Java SDK lacks four APIs that `androidMain` uses: `PhoneAuthOptions`, `AuthResult.getCredential()`, `AuthResult.getAdditionalUserInfo()`/`AdditionalUserInfo`, and `ActionCodeSettings.Builder.setLinkDomain()`. Tracked in GitLiveApp/firebase-java-sdk#68; when that lands the fork can be deleted and `srcDir` sharing restored.
