# GitHub Copilot Instructions — firebase-kotlin-sdk

## Project Overview

This is a **Kotlin-first, multiplatform SDK for Firebase**. It wraps the official Firebase platform SDKs (Android, iOS, JS, JVM) behind a unified Kotlin common API, enabling Firebase to be used directly from shared Kotlin Multiplatform (KMP) source sets targeting **Android**, **iOS**, **Desktop (JVM)**, and **Web (JS)**.

All modules are published under the `dev.gitlive` group ID (e.g. `dev.gitlive:firebase-firestore`).

---

## Architecture

### Module Structure

Each Firebase product is a separate Gradle module (e.g. `firebase-auth`, `firebase-firestore`, `firebase-database`). Inside each module the source is split by KMP targets:

```
src/
  commonMain/   ← shared public API (Kotlin)
  androidMain/  ← wraps Firebase Android SDK (and is also used as the physical dir for `jvmMain` in some modules, e.g. firebase-firestore)
  appleMain/    ← shared Apple targets (iOS/tvOS/macOS) wrapping Firebase iOS SDK via Kotlin/Native
  jsMain/       ← wraps Firebase JS SDK
  jvmMain/      ← JVM desktop/server target (may map to src/androidMain/kotlin in some modules)
  commonTest/   ← shared tests
  androidTest/
  appleTest/
  jsTest/
  jvmTest/
```

The `commonMain` source set defines the **public API**. Platform-specific source sets contain the `actual` implementations that delegate to the respective native SDK.

### Shared modules

- `firebase-app` — `FirebaseApp` and `Firebase` object
- `firebase-common` — shared types, serialization helpers, `FirebaseEncoder`/`FirebaseDecoder`
- `firebase-common-internal` — internal utilities not part of the public API

---

## Kotlin-First Design Principles

These principles **must** be followed in all new and modified code.

### 1. Suspend functions instead of callbacks or Tasks

Async operations that return a single value use `suspend fun`. Never use callbacks, `Task`, `Promise`, or listener patterns in `commonMain`.

```kotlin
// ✅ Correct
suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult

// ❌ Wrong
fun signInWithEmailAndPassword(email: String, password: String, callback: (AuthResult) -> Unit)
```

### 2. `Flow` instead of listeners

Streams of values use `kotlinx.coroutines.flow.Flow`. The flow should be cold — a new listener is registered on collection and removed on cancellation/completion.

```kotlin
// ✅ Correct
val snapshots: Flow<DocumentSnapshot>

// ❌ Wrong
fun addSnapshotListener(listener: (DocumentSnapshot) -> Unit): ListenerRegistration
```

### 3. Default arguments instead of the Builder pattern

Prefer Kotlin default arguments over builder classes. When the upstream Android SDK uses a Builder, provide a Kotlin-idiomatic overload with default arguments **in addition to** accepting the built object (for API compatibility).

```kotlin
// ✅ Correct
suspend fun updateProfile(displayName: String? = null, photoURL: String? = null)

// ❌ Avoid as the sole API
suspend fun updateProfile(request: UserProfileChangeRequest)
```

### 4. Infix notation for query operators

Firestore and Database query operators use infix functions inside a `where { }` builder lambda.

```kotlin
citiesRef.where { "state" equalTo "CA" }
citiesRef.where { "regions" contains "west_coast" }
citiesRef.where {
    all(
        "state" equalTo "CA",
        any("capital" equalTo true, "population" greaterThanOrEqualTo 1_000_000)
    )
}
```

### 5. Operator overloading where natural

Use operator overloading where semantics are obvious (e.g. callable HTTP Functions via `invoke`).

---

## Serialization

The SDK uses **`kotlinx.serialization`** throughout. Never use platform-specific serialization mechanisms in `commonMain`.

- Custom classes passed to/from Firebase must be annotated with `@Serializable`.
- Always accept an explicit `SerializationStrategy`/`DeserializationStrategy` parameter alongside a reified/inferred overload.
- `encodeDefaults` defaults to `true`; allow it to be overridden via a `buildSettings` lambda.
- Support `serializersModule` for contextual and polymorphic serialization.
- Use `@FirebaseClassDiscriminator` (defined in `firebase-common`) on sealed classes to control the type discriminator field name.
- Special sentinel values (`ServerValue.TIMESTAMP`, `Timestamp.ServerTimestamp`, `FieldValue.serverTimestamp`) must remain serializable.
- For Firestore update operations, provide an `updateFields` builder that allows per-field serializer overrides.

---

## API Compatibility Goal

The target is **near binary compatibility** with the [Firebase Android SDK Kotlin API](https://firebase.google.com/docs/reference/kotlin/packages):

- Match class names, function names, and parameter names from the Android SDK.
- Package imports should be the **only change** needed when porting Android code: `com.google.firebase` → `dev.gitlive.firebase`.
- When an Android SDK API is Java-first (uses builders, callbacks, etc.), provide **both** the Android-compatible form *and* a Kotlin-idiomatic overload.
- When the Android SDK API is already Kotlin-first, simply match it.

---

## Accessing the Underlying Platform SDK

Each wrapper class exposes the underlying native SDK object via extension properties:

- `.android` — Firebase Android SDK object (also used for JVM via `firebase-java-sdk`)
- `.ios` — Firebase iOS SDK object (Kotlin/Native)
- `.js` — Firebase JS SDK object

These are only accessible from the respective platform source sets. Do **not** use them in `commonMain`.

---

## Platform-Specific Notes

### Android
- Some modules (e.g. `firebase-config`) require **Core library desugaring** for `minSdk < 26`.
- Access the underlying Android object via the `.android` extension property in `androidMain`.

### iOS
- The Firebase iOS SDK is **not** a transitive dependency — consuming projects must link it via CocoaPods or SPM.
- Tests need the relevant Firebase pods in the `cocoapods` block of `build.gradle.kts`.

### JVM / Desktop
- Uses [`firebase-java-sdk`](https://github.com/GitLiveApp/firebase-java-sdk), which mirrors the Android SDK API.
- Requires additional initialization compared to mobile targets (see `firebase-java-sdk` docs).
- Accessed via the `.android` extension property (same as Android).

---

## Documentation

- Every **public** class, function, and property must have **KDoc**.
- Use [`@param`](https://kotlinlang.org/docs/kotlin-doc.html#param-name), [`@return`](https://kotlinlang.org/docs/kotlin-doc.html#return), and [`@throws`](https://kotlinlang.org/docs/kotlin-doc.html#throws-exception) tags where applicable.
- Published docs live at [gitliveapp.github.io/firebase-kotlin-sdk](https://gitliveapp.github.io/firebase-kotlin-sdk/).

---

## Code Style

- Follow the **IntelliJ Kotlin code style**.
- Run `./gradlew formatKotlin` to auto-format before committing.
- Run `./gradlew lintKotlin` to validate style.
- No `GlobalScope` usage (except in comments/examples showing what *not* to do).

---

## Binary API Validation

This library tracks its public binary API. After any public API change:

```bash
./gradlew apiDump
```

Commit the updated `.api` files alongside your code changes.

---

## Testing

Tests live in `commonTest` (shared) and platform-specific test source sets. A Firebase emulator must be running for integration tests:

```bash
# Inside the /test directory
firebase emulators:start
```

Run tests per platform:

```bash
./gradlew connectedAndroidTest        # requires running emulator
./gradlew iosSimulatorArm64Test       # Apple Silicon
./gradlew iosX64Test                  # Intel Mac
./gradlew jsNodeTest
./gradlew jvmTest
```

---

## What Copilot Should Avoid

- Do **not** generate Java-style callback APIs in `commonMain`.
- Do **not** use `runBlocking` in production SDK code.
- Do **not** add platform-specific imports to `commonMain` source files.
- Do **not** use `GlobalScope` — callers manage coroutine scope.
- Do **not** skip KDoc on public API members.
- Do **not** use `lateinit var` for public API properties; prefer `val` or nullable types with proper initialization.
- Do **not** hardcode serializers when a reified/inferred overload is possible.
