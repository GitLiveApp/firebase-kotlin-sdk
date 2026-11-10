# Firebase Notes: an Android app ready to be ported to Kotlin Multiplatform

A small notes app written against the [Firebase Android SDK](https://firebase.google.com/docs/android/setup), the
starting point of a demonstration of porting a Firebase Android app to Kotlin Multiplatform with the
[Firebase Kotlin SDK](../../README.md). It uses ten Firebase products the way an Android app does, and every line that
has to change in the port carries a `// KMP:` comment saying what it becomes. The [migration guide](../../documentation/migrate-from-android.md)
explains the rules behind those comments.

The app signs in anonymously, keeps the user's notes in Cloud Firestore, uploads each note's text to Cloud Storage,
calls a Cloud Function, reads its limits from Remote Config, and reports events, failures, traces, the installation id
and the messaging token through Analytics, Crashlytics, Performance Monitoring, Installations and Cloud Messaging.

## Running it

The app is configured in code (`NotesFirebase`) against the Firebase emulators, so it needs no `google-services.json`.
Start the emulators from the repository's `test` directory, whose `firebase.json` declares them and whose `functions`
hold the `echo` function the app calls, and run the app on an Android emulator, which reaches them on `10.0.2.2`:

```
cd ../../test && npm --prefix functions install && npx firebase-tools emulators:start --project fir-kotlin-sdk
./gradlew :app:installDebug
```

Set `NotesFirebase.EMULATOR_HOST` to `null` to use a real project instead; the options then need your own project's values.

## How the code is laid out for the port

The sources are already split the way the Kotlin Multiplatform project will be:

| Directory | Today | After the port |
| --- | --- | --- |
| `app/src/main/kotlin/dev/gitlive/notes/shared/` | the Firebase code, with no Android UI in it | `shared/src/commonMain/kotlin/dev/gitlive/notes/shared/`, compiled for Android, iOS, JS, Wasm and the JVM |
| `app/src/main/kotlin/dev/gitlive/notes/shared/platform/` | the Firebase code only Android can have | `shared/src/androidMain/…/platform/`, with `expect` declarations in `commonMain` where common code calls it |
| `app/src/main/kotlin/dev/gitlive/notes/` | `NotesApplication` and `MainActivity` | the Android app, unchanged, depending on `:shared` |

The build files carry the same comments: the Firebase BoM and `com.google.firebase:firebase-<module>` dependencies of
`app/build.gradle.kts` become `dev.gitlive:firebase-<module>` in the shared module's `commonMain`, and the
kotlinx.serialization plugin joins the shared module so the model class can be `@Serializable`.

## What changes, by kind

Most of the Firebase code moves without edits: `FirebaseApp.getApps(context)` and `initializeApp(context, options)`,
`useEmulator`, anonymous and email sign-in, the auth state listener, `collection().document().update(field, value)`,
`delete()`, `FieldValue.serverTimestamp()`, `orderBy` and `addSnapshotListener(listener)`, `putBytes`, `getBytes` and
`getDownloadUrl`, `getHttpsCallable().call(data)`, `setConfigSettingsAsync`, `fetchAndActivate`, `getLong` and
`getString`, `logEvent(name, Bundle)` with the `Event` and `Param` constants, `setUserId` and `setUserProperty`,
`log`, `setCustomKey` and `recordException`, `newTrace().start()`/`stop()`, `FirebaseInstallations.getId()`,
`isAutoInitEnabled`, `getToken()` and `subscribeToTopic`, and `Task.await()` on all of them. Each kind of change the
compiler will ask for appears at least once:

| Kind of change | Where | What it becomes |
| --- | --- | --- |
| The dependency | `app/build.gradle.kts` | `api("dev.gitlive:firebase-<module>")` in the shared module's `commonMain`, so the app still sees the Firebase types it uses; the BoM and `kotlinx-coroutines-play-services` go |
| A member deprecated with an error because it maps a class by reflection | `NotesRepository.add`, `save` | `addData`, `setData` from `com.google.firebase.serialization` |
| A class-taking read that is not declared, as `java.lang.Class` is JVM-only | `NotesRepository.load`, `listen` | the reified `toObject<Note>()`, `toObjects<Note>()` |
| The mapping annotations | `Note` | `@Serializable`, kotlinx.serialization's `@SerialName` and `@Transient`, and `@DocumentId`/`@ServerTimestamp` from `com.google.firebase.serialization` |
| A member deprecated with an error because it takes or returns a JVM type | `NotesRepository.createdOn`, `NoteAttachments.upload`, `NotesBackend.echo` | `toKotlinInstant()`, `putStream(RawSource)`, `setTimeout(Duration)` |
| An Android resource in the API | `NotesSettings.load` | `setDefaultsAsync(Map)` |
| `Task<Void>` named in a signature | `NotesRepository.save` | `Task<Nothing?>`; inferred types, as in `rename`, need nothing |
| An Android type in the signature, so the member is not declared | `platform/ActivityScopedNotes.kt` | stays in `androidMain` (its read still becomes `toObjects<Note>()`, as the reflective read ignores the serialization annotations), or the plain `addSnapshotListener(listener)` with `remove()` in `onStop` |
| A feature the iOS and JS SDKs lack, so the member is not declared | `platform/MessagingExports.kt` | an `expect` function in `commonMain` whose Android `actual` keeps the call and whose other actuals are empty |
| An Android component | `platform/NotesMessagingService.kt` | stays in `androidMain`, declared in the Android manifest as before |

Two things the comments rely on. `android.content.Context` and the subset of `android.os.Bundle` that analytics
events use are provided in common code, so `FirebaseAnalytics.getInstance(context)`, `logEvent(name, Bundle)` and the
`Context` parameters keep their Android signatures; the platforms without a context pass the placeholder `Context()`.
And the common declarations are Kotlin: a Java getter the Android SDK exposes as a method, such as `getToken()` or
`getId()`, is a function in common code too, so the code calls it as one rather than through Kotlin's synthetic
property syntax (`messaging.token`), which only exists for Java methods.
