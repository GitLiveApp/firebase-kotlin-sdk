# Linking the Firebase iOS SDK (SwiftPM)

The Firebase Kotlin SDK is distributed as **Maven Central klibs** and consumed by a Kotlin
Multiplatform (KMP) app, whose iOS framework is then integrated into an Xcode project. On iOS the
GitLive klibs call the Firebase Obj-C API through cinterop; the official Firebase iOS SDK is **not
bundled** into the klibs. Instead, each module records the `firebase-ios-sdk` SwiftPM dependency it
needs and **publishes that as metadata in its Maven module**, so a downstream KMP consumer inherits
it transitively — no manual re-declaration.

## How it works (verified)

Kotlin 2.4's SwiftPM integration publishes the declared `swiftPMDependencies` into the KMP Gradle
publication:

- The root Gradle module metadata exposes a **`swiftPMDependenciesMetadataElements`** variant
  (usage `swiftPMDependenciesMetadata`) referencing a **`<module>-<version>-swiftpm-metadata.json`**
  artifact. For example `firebase-app`'s artifact declares `firebase-ios-sdk` @ `11.8.0`, product
  `FirebaseCore`, deployment targets, and `isModulesDiscoveryEnabled = false`.
- A downstream KMP module that depends on `dev.gitlive:firebase-*` from Maven resolves this via its
  `swiftPMDependenciesMetadataClasspath`, aggregating the SwiftPM dependencies across the whole
  dependency graph — **without declaring any `swiftPMDependencies` of its own**. (Verified: a
  consumer depending only on `dev.gitlive:firebase-app` produced an aggregated
  `build/kotlin/swiftPMDependenciesMetadataForLockFiles` listing `firebase-ios-sdk` / `FirebaseCore`
  / `11.8.0`.)
- The Kotlin Gradle plugin then generates a synthetic Swift package containing all transitive
  SwiftPM dependencies and provides the machine code when linking the app's framework / running
  Kotlin/Native tests (`generateSyntheticLinkageSwiftPMImportProject…`, `integrateEmbedAndSign`,
  `integrateLinkagePackage`).

So the Firebase native dependency **flows across the Maven boundary automatically** — the consuming
app does not re-declare `firebase-ios-sdk`.

## What a downstream KMP app does

1. **Depend on the SDK via Maven** as usual — e.g. `implementation("dev.gitlive:firebase-auth:<v>")`.
   Nothing extra is required to obtain the Firebase iOS SDK; it is inherited from the SDK's published
   SwiftPM metadata.
2. **Integrate your shared framework into Xcode with _direct integration_.** Kotlin offers two ways
   to get a KMP framework into Xcode; for an app that consumes this SDK, only one currently works:

   - ✅ **[Direct integration](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html)**
     — add the `embedAndSignAppleFrameworkForXcode` run-script build phase (KGP exposes the
     SwiftPM-aware `integrateEmbedAndSign` / `integrateLinkagePackage` tasks). On each build Gradle
     compiles, resolves the inherited `firebase-ios-sdk`, generates the synthetic Swift package, and
     embeds/signs the framework. Setup steps and the exact script are in that guide (and the
     [CocoaPods→SwiftPM migration guide](https://kotlinlang.org/docs/multiplatform/multiplatform-cocoapods-spm-migration.html));
     background on SwiftPM import is [here](https://kotlinlang.org/docs/multiplatform/multiplatform-spm-import.html).
   - ❌ **[Remote / SwiftPM export](https://kotlinlang.org/docs/multiplatform/multiplatform-spm-export.html)**
     (packaging your shared module as its own Swift package via an exported XCFramework +
     `Package.swift`) is **not currently supported** for apps consuming this SDK: your shared module
     inherits `firebase-ios-sdk` as a SwiftPM dependency, and Kotlin does not yet support exporting a
     module that *uses* SwiftPM import as a Swift package
     ([KT-84420](https://youtrack.jetbrains.com/issue/KT-84420)). Use direct integration until that
     lands.
3. **Build your shared framework as static** — set `isStatic = true` on your `binaries.framework`.
   Firebase's SwiftPM products are static libraries; a **dynamic** shared framework produces
   `@rpath/…framework` load commands that aren't satisfied at runtime, so the app crashes with a
   `dyld: Library not loaded` error. `isStatic = true` embeds the symbols and defers Firebase linkage
   to the final Xcode link.

**Requirements:** Kotlin **2.4+** and Xcode **26.2+** (the SwiftPM import/integration toolchain;
Xcode 27 betas are currently incompatible with KGP 2.4).

### Known gotchas

- **iOS/macOS/tvOS test tasks.** The Kotlin/Native test runner links Firebase outside the Xcode
  integration context, so `…Test` tasks that touch Firebase symbols may fail to resolve them when run
  standalone. If you hit this, run the affected tests through the Xcode-integrated build, or disable
  the pure-Gradle iOS test tasks that require Firebase.
- **Deployment target.** This SDK targets **iOS 13 / tvOS 13 / macOS 10.15**, at or above
  firebase-ios-sdk 11.8's own declared minimums (iOS 12 / tvOS 13 / macOS 10.15). Your app's
  deployment target must be **≥** these. (Newer Firebase/Xcode combinations may require a higher
  floor — e.g. iOS 15/16 — so check the Firebase release notes if you upgrade `firebase-ios-sdk`.)
- **Don't mix package managers for Firebase.** All Firebase products share transitive C/C++
  dependencies (gRPC/abseil/leveldb/BoringSSL/nanopb). Linking some via SwiftPM and others via
  CocoaPods duplicates those symbols and causes `dyld` crashes — use one mechanism for the whole
  Firebase suite.

### Optional: pin or extend Firebase yourself

If the app needs a specific `firebase-ios-sdk` version or additional Firebase products beyond what
the SDK modules pull in, it can declare its own `swiftPMDependencies` in its shared module, which is
merged with the inherited ones. Keep the version aligned with the SDK's (`firebase-ios-sdk` in
`gradle/libs.versions.toml`) to avoid Obj-C symbol/ABI drift. Product → Clang module mapping (needed
only when declaring your own with `discoverClangModulesImplicitly = false`, since several products
expose Obj-C via an `*Internal` module):

| SPM product           | `importedClangModules`                 |
|-----------------------|----------------------------------------|
| FirebaseCore          | `FirebaseCore`                         |
| FirebaseAnalytics     | `FirebaseAnalytics`                    |
| FirebaseAuth          | `FirebaseAuth`, `FirebaseAuthInternal` |
| FirebaseRemoteConfig  | `FirebaseRemoteConfigInternal`         |
| FirebaseCrashlytics   | `FirebaseCrashlytics`                  |
| FirebaseDatabase      | `FirebaseDatabaseInternal`             |
| FirebaseFirestore     | `FirebaseFirestoreInternal`            |
| FirebaseFunctions     | `FirebaseFunctions`                    |
| FirebaseInstallations | `FirebaseInstallations`                |
| FirebaseMessaging     | `FirebaseMessaging`                    |
| FirebasePerformance   | `FirebasePerformance`                  |
| FirebaseStorage       | `FirebaseStorage`                      |

## Notes

- The GitLive SDK ships no `Package.swift` or podspecs; it is Maven-only, and the Firebase linkage
  travels via the published SwiftPM metadata described above.
- Resolving `firebase-ios-sdk` pulls its full transitive source graph (gRPC/abseil/leveldb/
  BoringSSL/nanopb for Firestore), which is large — budget disk/time on CI accordingly.
- End-to-end verified here: the publication side (`swiftPMDependenciesMetadataElements` +
  `swiftpm-metadata.json`) and transitive resolution on the consumer side. A full sample-app Xcode
  link was not exercised; the `integrate*` tasks that perform it are provided by the Kotlin Gradle
  plugin.
