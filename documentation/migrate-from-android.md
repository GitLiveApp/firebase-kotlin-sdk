# Migrating from the Firebase Android SDK to the Firebase Kotlin SDK

This guide walks through moving code written against the [Firebase Android SDK](https://firebase.google.com/docs/android/setup)
into the shared module of a Kotlin Multiplatform project, so that the same code runs on Android, iOS, JS and the JVM.
It follows the shape of JetBrains' [Migrating a Jetpack Compose app to Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform/migrate-from-android.html)
guide: a checklist, then the migration step by step, with the compiler leading the way.

The Firebase Kotlin SDK mirrors the Android SDK's `com.google.firebase.*` API in common code: same packages, classes,
member names and shapes (`Task` results, listener interfaces, builders). Your imports don't change. On Android and the
JVM the SDK adds nothing to your app: it depends on the official Firebase Android SDK, and your code keeps binding to
the real classes exactly as before. On iOS and JS the same declarations are implemented on top of the Firebase iOS SDK
and the Firebase JS SDK.

> This guide covers the `com.google.firebase` API only, and the modules migrated so far: `firebase-app` (`FirebaseApp`,
> `FirebaseOptions`, `Timestamp`, the exceptions, `Task` and `Task.await()`) and `firebase-installations`. The Kotlin-first
> `dev.gitlive.firebase` API, which the SDK also provides, is described in the [README](../README.md#kotlin-first-design).

## Checklist for a migration

Most Firebase code moves to `commonMain` unchanged. Before you start, look for the few things that cannot cross the
platform boundary; the compiler will find them for you in step 3, but it helps to know what to expect.

1. **Android types in your own signatures.** `android.content.Context`, `java.util.Date` or `java.time.Instant` in a
   class you are moving must become common types: the SDK takes the context as `Any?` and time as `kotlin.time.Instant`.
2. **Members that take or return an Android type.** `FirebaseApp.initializeApp(Context)`, `getApps(Context)`,
   `getApplicationContext()`, `FirebaseOptions.fromResource(Context)`, `Timestamp(Date)`, `Timestamp(Instant)`,
   `toDate()` and `toInstant()` exist in common code but are deprecated with an error whose message names the
   replacement, and where the replacement is a drop-in expression the IDE quick-fix applies it. Android code in
   `androidMain` keeps calling them as before.
3. **`Parcelable`.** `Timestamp` is `Parcelable` on Android only. Passing it through an `Intent` or `Bundle` stays in
   `androidMain`; a `@Parcelize` class of your own that holds a `Timestamp` field keeps working in common code, because
   the Parcelize plugin only runs in the Android compilation.
4. **`Tasks` static helpers.** `Tasks.forResult`, `Tasks.await` and friends are not mirrored. Use `TaskCompletionSource`
   to build a `Task`, and `kotlinx.coroutines.tasks.await` to wait for one.
5. **`Task<Void>`.** Common code spells it `Task<Nothing?>`. A property or parameter you declared as `Task<Void>`
   needs the new spelling; call sites that never named the type are unaffected.

Everything else in the two modules, including every listener, continuation, builder and static accessor, is available
with the same signature. Each module lists exactly which Android SDK members it provides in `api/android-sdk-compat.txt`
([firebase-app](../firebase-app/api/android-sdk-compat.txt), [firebase-installations](../firebase-installations/api/android-sdk-compat.txt)).

## Step 1: Prepare the environment

Set up a Kotlin Multiplatform project with a shared module, following [Create your first cross-platform app](https://kotlinlang.org/docs/multiplatform/multiplatform-create-first-app.html)
if you don't have one yet. The Firebase code moves into that shared module.

## Step 2: Replace the dependency

In the Android app, the Firebase Android SDK is declared through the BoM:

```kotlin
// app/build.gradle.kts, before
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-installations")
}
```

In the shared module, depend on the Firebase Kotlin SDK module instead, in `commonMain`:

```kotlin
// shared/build.gradle.kts, after
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("dev.gitlive:firebase-installations:2.7.0") // brings dev.gitlive:firebase-app
        }
    }
}
```

What this changes per platform:

- **Android**: the module depends on the official `com.google.firebase:firebase-installations` and `firebase-common`, so
  they are still on your app's classpath, and nothing else from `com.google.*` is added. Keep the Google Services Gradle
  plugin and `google-services.json` on the Android app as before; the BoM in the app module stays optional.
- **iOS**: the Firebase iOS SDK is not linked transitively. Add the pods your app uses (`FirebaseCore`,
  `FirebaseInstallations`) through CocoaPods or Swift Package Manager, and `GoogleService-Info.plist` to the app.
- **JS**: the Firebase JS SDK is an npm dependency of the module; nothing to add.
- **JVM**: the [Firebase Java SDK](https://github.com/GitLiveApp/firebase-java-sdk) is used, see [Initialization](../README.md#initialization).

## Step 3: Move the code and let the compiler guide you

Take a class from the Android app that uses the Firebase installations API. This one initialises Firebase if the
Google Services plugin has not, exposes the installation id and auth token, and stamps records with a `Timestamp`:

```kotlin
// app/src/main/kotlin/com/example/FirebaseInstallationRepository.kt, before
package com.example

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.Timestamp
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.installations.InstallationTokenResult
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseInstallationRepository(private val context: Context) {

    fun ensureInitialized(): FirebaseApp =
        FirebaseApp.getApps(context).firstOrNull()
            ?: FirebaseApp.initializeApp(context, FirebaseOptions.fromResource(context)!!)

    fun installationId(): Task<String> = FirebaseInstallations.getInstance().getId()

    suspend fun authToken(forceRefresh: Boolean = false): String {
        val result: InstallationTokenResult = FirebaseInstallations.getInstance().getToken(forceRefresh).await()
        return result.token
    }

    fun onInstallationId(onId: (String) -> Unit, onError: (Exception) -> Unit) {
        installationId()
            .addOnSuccessListener { onId(it) }
            .addOnFailureListener { onError(it) }
    }

    fun stamp(date: Date): Timestamp = Timestamp(date)

    fun toDate(timestamp: Timestamp): Date = timestamp.toDate()
}
```

Move the file to `shared/src/commonMain/kotlin/com/example/` and change the two Android types in its own signatures:
the constructor takes the platform context as `Any?`, and the time conversions use `kotlin.time.Instant`. Nothing else
is edited by hand:

```kotlin
// shared/src/commonMain/kotlin/com/example/FirebaseInstallationRepository.kt, after moving
class FirebaseInstallationRepository(private val context: Any?) {
    // ...unchanged...
    fun stamp(instant: Instant): Timestamp = Timestamp(instant)
    fun toInstant(timestamp: Timestamp): Instant = timestamp.toInstant()
}
```

> `kotlin.time.Instant` is still experimental in Kotlin 2.2, so opt in with `kotlin.time.ExperimentalTime` in the
> module's compiler options, or annotate the file with `@OptIn(ExperimentalTime::class)`.

Now compile the shared module for any target other than Android, for example `./gradlew :shared:compileKotlinIosSimulatorArm64`.
Every remaining Android-only member is reported as an error that tells you what to use instead:

```
e: FirebaseInstallationRepository.kt: 'fun getApps(context: Any?): List<FirebaseApp>' is deprecated. getApps takes an android.content.Context and can only be called from Android code; use Firebase.getApps(context) from common code (the context is ignored on the other platforms).
e: FirebaseInstallationRepository.kt: 'fun initializeApp(context: Any?, options: FirebaseOptions): FirebaseApp' is deprecated. initializeApp takes an android.content.Context and can only be called from Android code; use Firebase.initialize(context) from common code (the context is ignored on the other platforms).
e: FirebaseInstallationRepository.kt: 'fun fromResource(context: Any?): FirebaseOptions?' is deprecated. fromResource takes an android.content.Context and can only be called from Android code; use Firebase.fromResource(context) from common code, which reads the platform's default configuration (the context is ignored on the other platforms).
e: FirebaseInstallationRepository.kt: Return type mismatch: expected 'Instant', actual 'Any'.
e: FirebaseInstallationRepository.kt: 'fun toInstant(): Any' is deprecated. toInstant returns a java.time.Instant, which is JVM-only; use toKotlinInstant(), or seconds and nanoseconds.
```

Note what is *not* reported. `Timestamp(instant)` compiles as it is: with a `kotlin.time.Instant` argument the call
resolves to the SDK's top-level `Timestamp(Instant)` function rather than the deprecated `Timestamp(Date)` constructor,
so changing the parameter type was the whole migration for that line. Everything else the compiler did not flag, the
`Task` API, the listeners, `getInstance()`, `InstallationTokenResult` and `kotlinx.coroutines.tasks.await`, already
works on every platform.

## Step 4: Apply the replacements

Each deprecation above carries a `ReplaceWith`, so in IntelliJ IDEA or Android Studio put the caret on the red call,
press `Alt+Enter` (`⌥⏎` on macOS) and choose **Replace with 'Firebase.initialize(context, options)'**, or run
**Code | Inspect Code** and apply the "Deprecated API usage" fixes in bulk. The imports are added for you, and the
return type mismatch disappears with the `toKotlinInstant()` fix.

The rule behind the replacements is the same every time: a member of a Java class that only needs its `Context`
widened moves onto the `Firebase` object under the same name (`Firebase.getApps`, `Firebase.fromResource`, and the
`Firebase.initialize` extensions the Android SDK already has), and the JVM time types get a `kotlin.time.Instant`
counterpart (`Timestamp(Instant)`, `Timestamp.toKotlinInstant()`). After the quick-fixes the class reads:

```kotlin
// shared/src/commonMain/kotlin/com/example/FirebaseInstallationRepository.kt, after
package com.example

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.fromResource
import com.google.firebase.getApps
import com.google.firebase.initialize
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.installations.InstallationTokenResult
import com.google.firebase.toKotlinInstant
import kotlinx.coroutines.tasks.await
import kotlin.time.Instant

class FirebaseInstallationRepository(private val context: Any?) {

    fun ensureInitialized(): FirebaseApp =
        Firebase.getApps(context).firstOrNull()
            ?: Firebase.initialize(context, Firebase.fromResource(context)!!)

    fun installationId(): Task<String> = FirebaseInstallations.getInstance().getId()

    suspend fun authToken(forceRefresh: Boolean = false): String {
        val result: InstallationTokenResult = FirebaseInstallations.getInstance().getToken(forceRefresh).await()
        return result.token
    }

    fun onInstallationId(onId: (String) -> Unit, onError: (Exception) -> Unit) {
        installationId()
            .addOnSuccessListener { onId(it) }
            .addOnFailureListener { onError(it) }
    }

    fun stamp(instant: Instant): Timestamp = Timestamp(instant)

    fun toInstant(timestamp: Timestamp): Instant = timestamp.toKotlinInstant()
}
```

This is exactly the code compiled while writing this guide, for JS, the JVM and both Android test compilations.

The replacements are real functions on every platform, not stubs: on Android `Firebase.getApps(context)` calls
`FirebaseApp.getApps(Context)` with the context you passed, on iOS it lists the configured `FIRApp`s and ignores the
context, and `Firebase.fromResource` reads `google-services.json` resources on Android, `GoogleService-Info.plist` on iOS,
and returns `null` on JS, where there is no default configuration.

Two members have no multiplatform replacement:

- `FirebaseApp.getApplicationContext()` returns an Android `Context`. Code that needs it belongs in `androidMain`, where
  the real member is available unchanged; from common code, pass the context you already have.
- The `Parcelable` members of `Timestamp` (`writeToParcel`, `describeContents`, `CREATOR`) are not declared. See the checklist.

> Code left in `androidMain` never sees the deprecations: the Android compilation binds to the Firebase Android SDK's own
> classes, where `initializeApp(Context)` and the others are the real, undeprecated members.

## Step 5: Initialise Firebase on each platform

`ensureInitialized()` above is now callable from every platform's entry point, with whatever that platform has as a
context:

```kotlin
// androidMain: only needed without the Google Services plugin, which initialises the default app itself
FirebaseInstallationRepository(applicationContext).ensureInitialized()

// iosMain, from the app delegate or the SwiftUI App init (Firebase.fromResource reads GoogleService-Info.plist)
FirebaseInstallationRepository(null).ensureInitialized()

// jsMain: there is no default configuration, so pass options explicitly
Firebase.initialize(null, FirebaseOptions.Builder().setApiKey("AIza...").setApplicationId("1:846484016111:web:abc123").setProjectId("fir-kotlin-sdk").build())
```

The [Initialization](../README.md#initialization) section of the README has the full per-platform table, including the JVM.

## Step 6: Run on iOS and JS

Run the shared module's tests on the other targets to confirm the migrated code behaves the same, for example
`./gradlew :shared:iosSimulatorArm64Test :shared:jsTest`. Two behavioural differences to know about:

- `Task` listeners on iOS and JS run on the thread that completes the task, where the Android SDK posts them to the
  main thread. Code that touches UI from a listener should switch to the main dispatcher explicitly, or use `await()`
  from a coroutine on the main dispatcher.
- `InstallationTokenResult.tokenExpirationTimestamp` is `0` on JS, because the Firebase JS SDK returns only the token string.

## What's next

- New multiplatform code can use the Kotlin-first `dev.gitlive.firebase` API (suspend functions instead of `Task`,
  `Flow` instead of listeners), which is built on the same `com.google.firebase` layer; a `dev.gitlive` object exposes
  its `com.google` counterpart as `compat`. See [Kotlin-first design](../README.md#kotlin-first-design).
- The other Firebase modules are being migrated to the same structure one at a time. Until a module is, its Android
  SDK API is not available in common code and its `dev.gitlive` API is the multiplatform entry point.
- [Using the Firebase Android SDK API from common code](../README.md#using-the-firebase-android-sdk-api-from-common-code)
  in the README describes the rules of the compatibility layer and how its coverage is measured.
