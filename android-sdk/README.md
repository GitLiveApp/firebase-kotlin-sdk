# Relocated Firebase Android SDK

This directory republishes the Firebase Android SDK artifacts (and `dev.gitlive:firebase-java-sdk`, the JVM port
used by the `jvm` target) with every `com.google.firebase.*` class moved to the `dev.gitlive.firebase.android.*`
package, as `dev.gitlive.firebase.android:<artifactId>:<original version>`.

## Why

The Firebase Kotlin SDK declares its own `com.google.firebase.*` classes (the Android-SDK-compatible API layer,
see the root README) so that code written for the Firebase Android SDK compiles unchanged on every platform.
On Android those classes would clash with the real SDK, so the real SDK is embedded under another package and the
Android actuals wrap it. `FirebaseApp.android`, `FirebaseAuth.android` etc. return these relocated types.

## How

Each subproject is one artifact, configured entirely by its `build.gradle.kts`:

```kotlin
plugins { id("dev.gitlive.relocated-android-artifact") } // or dev.gitlive.relocated-jvm-artifact for plain jars
relocatedArtifact {
    original.set("com.google.firebase:firebase-installations") // version from the Firebase BoM, or group:artifact:version
    namespace.set("com.google.firebase.installations")         // AARs only: the manifest package of the original
}
```

The plugins (in `buildSrc/src/main/kotlin/relocate`) resolve the original artifact, rewrite it with ASM
(class names, descriptors, string constants, the SMAP debug attribute, `AndroidManifest.xml` component names and
`ComponentRegistrar` meta-data, consumer proguard rules) and rewrite the `@kotlin.Metadata` annotations and
`.kotlin_module` files with `kotlin-metadata-jvm`, so relocated Kotlin declarations (extension functions, companions,
default arguments) stay usable from Kotlin. AGP / the Java plugin then package the result.

Dependencies are derived from the original POM: `com.google.firebase` dependencies point at the sibling relocated
projects, everything else (Play Services, AndroidX, ...) is kept as-is with the original Firebase artifacts excluded
from its transitive graph. `play-services-tasks` is deliberately not relocated: `com.google.android.gms.tasks.Task` is
shared with the rest of the app. Three kinds of special cases are registered in `android-sdk/build.gradle.kts`:

- `extractOnlyArtifacts` (`play-services-basement`): an artifact outside the Firebase group that contains a few
  `com.google.firebase` classes (`FirebaseException`). The original stays a dependency; only those classes are
  republished relocated.
- `keepOriginalArtifacts` (`firebase-annotations`, `firebase-encoders*`): unrelocated Play Services / datatransport
  libraries reference these packages, which do not clash with this SDK, so the originals stay on the classpath next to
  the relocated copies.
- Play Services artifacts that contain or reference other Firebase classes (`play-services-measurement-api`,
  `play-services-measurement-sdk-api`) are republished fully relocated under this group: their `com.google.android.gms`
  classes keep their names, so an app must not also depend on the originals (e.g. through AdMob).

Useful tasks:

- `./gradlew :android-sdk:relocated-<artifact>:relocate` writes `build/relocated/` (classes.jar, manifest, report.txt).
- `./gradlew :android-sdk:verifyNoUnrelocatedReferences` fails if any third-party dependency still references
  `com.google.firebase` (it must then be added here as well).
- `./gradlew :android-sdk:publishAll` publishes every artifact (skipped by CI for versions already on Maven Central).

## Consequences to be aware of

- Any other library in an app that depends on `com.google.firebase:*` directly will bring the unrelocated SDK back
  and clash with this SDK's `com.google.firebase.*` classes.
- The Firebase Performance Gradle plugin instruments `com.google.firebase.perf` call sites and is not supported.
- Persisted state keyed by package-qualified names (e.g. the Installations FID stored in SharedPreferences) is not
  migrated, so an upgraded app gets a new installation id.
- String constants that start with `com.google.firebase.` are rewritten too (this is what keeps component discovery and
  Dynamite module descriptors working), including intent actions such as `com.google.firebase.MESSAGING_EVENT` that
  unrelocated Play Services code still sends with the original name. `firebase-messaging` therefore needs further work
  before it functions on top of the relocated SDK; `firebase-app` and `firebase-installations` are verified.
