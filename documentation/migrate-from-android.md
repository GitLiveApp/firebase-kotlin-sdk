# Migrating from the Firebase Android SDK to the Firebase Kotlin SDK

This guide walks through moving code written against the [Firebase Android SDK](https://firebase.google.com/docs/android/setup)
into the shared module of a Kotlin Multiplatform project, so that the same code runs on Android, iOS, JS, Wasm (wasmJs) and the JVM.
It follows the shape of JetBrains' [Migrating a Jetpack Compose app to Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform/migrate-from-android.html)
guide: a checklist, then the migration step by step, with the compiler leading the way.

The Firebase Kotlin SDK mirrors the Android SDK's `com.google.firebase.*` API in common code: same packages, classes,
member names and shapes (`Task` results, listener interfaces, builders). Your imports don't change. On Android and the
JVM the SDK adds nothing to your app: it depends on the official Firebase Android SDK, and your code keeps binding to
the real classes exactly as before. On iOS, JS and Wasm the same declarations are implemented on top of the Firebase iOS
SDK and the Firebase JS SDK.

> This guide covers the `com.google.firebase` API only. Every module of the SDK provides it: `firebase-app` (`FirebaseApp`,
> `FirebaseOptions`, `Timestamp`, the exceptions, `Task` and `Task.await()`), `firebase-installations`, `firebase-crashlytics`,
> `firebase-config`, `firebase-functions`, `firebase-analytics`, `firebase-auth`, `firebase-perf`, `firebase-messaging`,
> `firebase-storage`, `firebase-database` and `firebase-firestore`. The Kotlin-first `dev.gitlive.firebase` API, which the SDK
> also provides, is described in the [README](../README.md#kotlin-first-design).

> [samples/notes-android](../samples/notes-android) is an Android app written for this migration: its Firebase code is
> already laid out as the shared module will be, and every line the port changes carries a `// KMP:` comment with its
> replacement, one for each kind of change described below.

## Checklist for a migration

Most Firebase code moves to `commonMain` unchanged. Before you start, look for the few things that cannot cross the
platform boundary; the compiler will find them for you in step 3, but it helps to know what to expect.

1. **JVM types in your own signatures.** `java.util.Date` or `java.time.Instant` in a class you are moving must become
   `kotlin.time.Instant`, `java.util.concurrent.TimeUnit` a `kotlin.time.Duration`, `java.io.File` a kotlinx-io `Path`,
   `java.io.InputStream` a kotlinx-io `RawSource` and `java.nio.ByteBuffer` a `ByteArray`. `android.content.Context` can
   stay: the SDK provides the class in common code (on the platforms that have no context it is a placeholder that
   platform code creates with `Context()`), so the members that take or return a context keep the Android SDK's exact
   signatures, and `android.net.Uri` and the subset of `android.os.Bundle` that analytics events use are provided too.
2. **Members that take or return a JVM type.** `Timestamp(Date)`, `Timestamp(Instant)`, `toDate()`, `toInstant()`,
   `DocumentSnapshot.getDate`, `loadBundle(InputStream)`, `getFile(File)`, `putStream(InputStream)`,
   `setTimeout(Long, TimeUnit)` and the other members of this kind exist in common code but are deprecated with an error
   whose message names the replacement (`Timestamp(Instant)` with a `kotlin.time.Instant`, `getFile(Path)`,
   `putStream(RawSource)`, `setTimeout(Duration)`, and so on), and where the replacement is a drop-in expression the IDE
   quick-fix applies it. Android code in `androidMain` keeps calling them as before.
3. **Custom classes.** The Android SDK maps your model classes by reflection, which only exists on Android and the JVM, so the
   writes that take one (`set(Any)`, `update(Map)` and `add(Any)` on Firestore, `setValue(Any?)` and
   `updateChildren(Map)` on the database) are deprecated with an error, and the class-taking reads (`toObject(Class)`,
   `toObjects(Class)`, `getValue(Class)` and `getValue(GenericTypeIndicator)`) are not declared in common code at all,
   as `java.lang.Class` is JVM-only (see item 8), so they are reported as unresolved. The replacements map a
   `@Serializable` class with kotlinx.serialization on every platform: `setData`, `updateData` and `addData`, and the
   reified `toObject<T>()`, `toObjects<T>()`, `getField<T>()`, `dataObjects<T>()` and `getValue<T>()` in
   `com.google.firebase.serialization`. Plain values and maps work through them too. The SDK's mapping annotations
   (`@PropertyName`, `@Exclude`, `@DocumentId`, `@ServerTimestamp`, `@IgnoreExtraProperties`, `@ThrowOnExtraProperties`)
   are deprecated with a `ReplaceWith` towards kotlinx.serialization's `@SerialName` and `@Transient` and the
   `com.google.firebase.serialization` annotations of the same names. See [Custom classes](#custom-classes) below.
4. **`Parcelable`.** `Timestamp` is `Parcelable` on Android only. Passing it through an `Intent` or `Bundle` stays in
   `androidMain`; a `@Parcelize` class of your own that holds a `Timestamp` field keeps working in common code, because
   the Parcelize plugin only runs in the Android compilation.
5. **`Tasks` static helpers.** `Tasks.forResult`, `Tasks.await` and friends are not mirrored. Use `TaskCompletionSource`
   to build a `Task`, and `kotlinx.coroutines.tasks.await` to wait for one.
6. **`Task<Void>`.** Common code spells it `Task<Nothing?>`. A property or parameter you declared as `Task<Void>`
   needs the new spelling; call sites that never named the type are unaffected.
7. **Members the Firebase JS SDK has no counterpart for.** These are annotated `@UnsupportedOnJs`, an opt-in that warns
   until you accept, with `@OptIn(UnsupportedOnJs::class)`, that the member does nothing on JS and Wasm, or fails where it
   must return a value. Storage's `putFile(Uri)` and `getFile(Uri)` are an example: the JS SDK uploads and downloads
   bytes, not files. The deprecation and opt-in messages, and each member's documentation, say what happens.
8. **Members of features the iOS and JS SDKs lack.** Play Games sign-in, the TOTP secret's parameters, the Android-only
   messaging settings, storage's chunk retry count and upload resumption, `FirebaseMessagingService` and the streaming
   `StreamResponse` are declared with an error-level deprecation whose message says the member is Android-only and where
   the call belongs (`androidMain`), so common code using one fails with that message. They are listed in each module's
   `api/android-sdk/exclusions.txt` as `@unavailable` and left out of the coverage count.
9. **Members that are not declared at all.** A member bound to an Android type the other platforms cannot have (the
   `Activity`-taking sign-in flows, the `Executor` overloads of the listener methods, `Parcelable`, `java.lang.Class`) is
   left out, so the compiler reports it as unresolved rather than deprecated. Each module's `api/android-sdk/exclusions.txt`
   lists them as `@platform`, with the type.

Everything else, including every listener, continuation, builder and static accessor, is available with the same
signature. Each module lists exactly which Android SDK members it provides in its `api/android-sdk-compat.txt` (for
example [firebase-firestore](../firebase-firestore/api/android-sdk-compat.txt)) and the members it leaves out, with the
reason, in its `api/android-sdk/exclusions.txt`.

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

In the shared module, depend on the Firebase Kotlin SDK module instead, in `commonMain`; the module names match the
Android SDK's (`firebase-installations`, `firebase-firestore`, `firebase-auth` and so on):

```kotlin
// shared/build.gradle.kts, after
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("dev.gitlive:firebase-installations:<version>") // brings dev.gitlive:firebase-app
        }
    }
}
```

A module whose code maps custom classes (Firestore, the database) also needs the kotlinx.serialization Gradle plugin on
the shared module, so that your model classes can be `@Serializable`.

What this changes per platform:

- **Android**: the module depends on the official `com.google.firebase:firebase-installations` and `firebase-common`, so
  they are still on your app's classpath, and nothing else from `com.google.*` is added. Keep the Google Services Gradle
  plugin and `google-services.json` on the Android app as before; the BoM in the app module stays optional.
- **iOS**: `firebase-ios-sdk` (`FirebaseCore`, `FirebaseInstallations`) is inherited transitively through the SwiftPM
  metadata the modules publish. Integrate the shared framework with direct integration as a static framework (see
  [Running on iOS](../README.md#running-on-ios) and [ios-firebase-linking.md](ios-firebase-linking.md)), and add
  `GoogleService-Info.plist` to the app. Don't also add Firebase through CocoaPods: mixing the two duplicates symbols.
- **JS and Wasm**: the Firebase JS SDK is an npm dependency of the module; nothing to add.
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

Move the file to `shared/src/commonMain/kotlin/com/example/` and change the one JVM type in its own signatures: the
time conversions use `kotlin.time.Instant`. The `Context` stays, `android.content.Context` resolves in common code.
Nothing else is edited by hand:

```kotlin
// shared/src/commonMain/kotlin/com/example/FirebaseInstallationRepository.kt, after moving
class FirebaseInstallationRepository(private val context: Context) {
    // ...unchanged...
    fun stamp(instant: Instant): Timestamp = Timestamp(instant)
    fun toInstant(timestamp: Timestamp): Instant = timestamp.toInstant()
}
```

> `kotlin.time.Instant` is stable since Kotlin 2.3 and this SDK line requires Kotlin 2.4, so no opt-in is needed unless
> you compile with an older `apiVersion`; then opt in with `kotlin.time.ExperimentalTime` in the module's compiler
> options, or annotate the file with `@OptIn(ExperimentalTime::class)`.

Now compile the shared module for any target other than Android, for example `./gradlew :shared:compileKotlinIosSimulatorArm64`.
Every remaining JVM-only member is reported as an error that tells you what to use instead:

```
e: FirebaseInstallationRepository.kt: Return type mismatch: expected 'Instant', actual 'Any'.
e: FirebaseInstallationRepository.kt: 'fun toInstant(): Any' is deprecated. toInstant returns a java.time.Instant, which is JVM-only; use toKotlinInstant(), or seconds and nanoseconds.
```

Note what is *not* reported. `FirebaseApp.getApps(context)`, `FirebaseApp.initializeApp(context, options)` and
`FirebaseOptions.fromResource(context)` compile unchanged: they take an `android.content.Context` on every platform.
`Timestamp(instant)` compiles as it is: with a `kotlin.time.Instant` argument the call resolves to the SDK's top-level
`Timestamp(Instant)` function rather than the deprecated `Timestamp(Date)` constructor, so changing the parameter type
was the whole migration for that line. Everything else the compiler did not flag, the `Task` API, the listeners,
`getInstance()`, `InstallationTokenResult` and `kotlinx.coroutines.tasks.await`, already works on every platform.

## Step 4: Apply the replacements

The deprecation above carries a `ReplaceWith`, so in IntelliJ IDEA or Android Studio put the caret on the red call,
press `Alt+Enter` (`⌥⏎` on macOS) and choose **Replace with 'toKotlinInstant()'**, or run **Code | Inspect Code** and
apply the "Deprecated API usage" fixes in bulk. The import is added for you, and the return type mismatch disappears
with it.

The rule behind the replacements is the same every time: a JVM time type gets a `kotlin.time.Instant` counterpart
(`Timestamp(Instant)`, `Timestamp.toKotlinInstant()`). After the quick-fix the class reads:

```kotlin
// shared/src/commonMain/kotlin/com/example/FirebaseInstallationRepository.kt, after
package com.example

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.Timestamp
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.installations.InstallationTokenResult
import com.google.firebase.toKotlinInstant
import kotlinx.coroutines.tasks.await
import kotlin.time.Instant

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

    fun stamp(instant: Instant): Timestamp = Timestamp(instant)

    fun toInstant(timestamp: Timestamp): Instant = timestamp.toKotlinInstant()
}
```

This is exactly the code compiled while writing this guide, for JS, the JVM and both Android test compilations.

The `Context` members are implemented on every platform: on Android and the JVM they are the real SDK statics with the
context you passed, on iOS `getApps` lists the configured `FIRApp`s and ignores the context, and `fromResource` reads
`google-services.json` resources on Android, `GoogleService-Info.plist` on iOS, and returns `null` on JS and Wasm, where
there is no default configuration. `getApplicationContext()` returns the application context on Android and the JVM and
a placeholder `Context` elsewhere.

One group of members has no multiplatform replacement: the `Parcelable` members of `Timestamp` (`writeToParcel`,
`describeContents`, `CREATOR`) are not declared. See the checklist.

> Code left in `androidMain` never sees the deprecations: the Android compilation binds to the Firebase Android SDK's own
> classes, where `Timestamp(Date)` and the others are the real, undeprecated members.

## Custom classes

Firestore and Realtime Database code usually writes and reads model classes. The Android SDK maps them by reflection,
which only exists on Android and the JVM, so this repository compiles for those two targets but no other:

```kotlin
// shared/src/commonMain/kotlin/com/example/BookRepository.kt, moved from the Android app
package com.example

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import kotlinx.coroutines.tasks.await

data class Book(
    @get:PropertyName("book_title") val title: String = "",
    val rating: Double = 0.0,
)

class BookRepository(private val firestore: FirebaseFirestore) {
    suspend fun save(id: String, book: Book) = firestore.collection("books").document(id).set(book).await()
    suspend fun load(id: String): Book? = firestore.collection("books").document(id).get().await().toObject(Book::class.java)
}
```

```
e: BookRepository.kt: 'fun set(data: Any): Task<Nothing?>' is deprecated. set maps a custom class with the Android SDK's reflection, which only exists on Android and the JVM; setData maps a @Serializable class, or a plain map, with kotlinx.serialization on every platform
e: BookRepository.kt: Unresolved reference 'toObject' on receiver of type 'DocumentSnapshot'.
e: BookRepository.kt: Unresolved reference 'java' on receiver of type 'KClass<Book>'.
```

The write is deprecated with the message above; the class-taking read is unresolved, because neither `toObject(Class)`
nor `Book::class.java` exists off the JVM. Once those are fixed the annotation gets its warning:

```
w: BookRepository.kt: 'annotation class PropertyName : Annotation' is deprecated. Only the Android SDK's reflective mapping reads @PropertyName; kotlinx.serialization's @SerialName names the property on every platform.
```

The fix is the same for every such member: make the class `@Serializable`,
replace the write with its `Data` counterpart and the class-taking read with the reified one from
`com.google.firebase.serialization`, and apply the `ReplaceWith` of the annotation. For the annotation, the quick-fix in
step 4 does it; the members need the new name typed, and the IDE offers the import:

```kotlin
// shared/src/commonMain/kotlin/com/example/BookRepository.kt, after
package com.example

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.serialization.setData
import com.google.firebase.serialization.toObject
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Book(
    @SerialName("book_title") val title: String = "",
    val rating: Double = 0.0,
)

class BookRepository(private val firestore: FirebaseFirestore) {
    suspend fun save(id: String, book: Book) = firestore.collection("books").document(id).setData(book).await()
    suspend fun load(id: String): Book? = firestore.collection("books").document(id).get().await().toObject<Book>()
}
```

The mapping follows the Android SDK's rules on every platform, Android included: the stored field name is the property's
serial name, a `Timestamp`, `GeoPoint`, `VectorValue`, `DocumentReference` or `Blob` property needs `@Contextual`, and
`@ServerTimestamp`, `@DocumentId`, `@IgnoreExtraProperties` and `@ThrowOnExtraProperties` come from
`com.google.firebase.serialization`. A property the Android SDK stored under its getter name (`isActive` as `active`)
keeps that name with `@SerialName("active")`. Plain values and maps work through `setData` and the reified reads as
well, so `set(mapOf(...))` becomes `setData(mapOf(...))` with no other change.

## Step 5: Initialise Firebase on each platform

`ensureInitialized()` above is now callable from every platform's entry point. Android passes its context; the other
platforms have none, so they create the placeholder the SDK provides, `android.content.Context()`:

```kotlin
// androidMain: only needed without the Google Services plugin, which initialises the default app itself
FirebaseInstallationRepository(applicationContext).ensureInitialized()

// iosMain, from the app delegate or the SwiftUI App init (FirebaseOptions.fromResource reads GoogleService-Info.plist)
FirebaseInstallationRepository(Context()).ensureInitialized()

// jsMain and wasmJsMain: there is no default configuration, so pass options explicitly
FirebaseApp.initializeApp(Context(), FirebaseOptions.Builder().setApiKey("AIza...").setApplicationId("1:846484016111:web:abc123").setProjectId("fir-kotlin-sdk").build())
```

The [Initialization](../README.md#initialization) section of the README has the full per-platform table, including the JVM.

## Step 6: Run on iOS, JS and Wasm

Run the shared module's tests on the other targets to confirm the migrated code behaves the same, for example
`./gradlew :shared:iosSimulatorArm64Test :shared:jsTest :shared:wasmJsNodeTest`. Behavioural differences to know about:

- `Task` listeners on iOS, JS and Wasm run on the thread that completes the task, where the Android SDK posts them to the
  main thread. Code that touches UI from a listener should switch to the main dispatcher explicitly, or use `await()`
  from a coroutine on the main dispatcher.
- `InstallationTokenResult.tokenExpirationTimestamp` is `0` on JS and Wasm, because the Firebase JS SDK returns only the token string.
- The `@UnsupportedOnJs` members do nothing, or fail, on JS and Wasm (see the checklist), and analytics is a no-op on the
  JVM, where the Firebase Java SDK has no analytics.
- Storage's `putFile` and `getFile` fail on JS and Wasm, where the SDK has no file operations; use `putBytes`, `getBytes`
  or `getDownloadUrl` there, or the `dev.gitlive` wrapper's `putFile` with a browser `File`.

## What's next

- New multiplatform code can use the Kotlin-first `dev.gitlive.firebase` API (suspend functions instead of `Task`,
  `Flow` instead of listeners), which is built on the same `com.google.firebase` layer; a `dev.gitlive` object exposes
  its `com.google` counterpart as `compat`. See [Kotlin-first design](../README.md#kotlin-first-design).
- Every module of the SDK provides its `com.google.firebase` API in common code, so the steps above apply to any
  Firebase code in your app; the checklist names the members that differ, and each module's
  `api/android-sdk-compat.txt` and `api/android-sdk/exclusions.txt` are the exact lists.
- [Using the Firebase Android SDK API from common code](../README.md#using-the-firebase-android-sdk-api-from-common-code)
  in the README describes the rules of the compatibility layer and how its coverage is measured.
