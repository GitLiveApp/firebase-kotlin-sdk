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
  artifact. For example `firebase-app`'s artifact declares `firebase-ios-sdk` @ `12.17.0`, product
  `FirebaseCore`, deployment targets, and `isModulesDiscoveryEnabled = false`. The declared version
  is a `from(...)` lower bound, so SwiftPM may resolve any newer 12.x minor or patch release, up to
  but not including 13.0.0.
- A downstream KMP module that depends on `dev.gitlive:firebase-*` from Maven resolves this via its
  `swiftPMDependenciesMetadataClasspath`, aggregating the SwiftPM dependencies across the whole
  dependency graph — **without declaring any `swiftPMDependencies` of its own**. (Verified: a
  consumer depending only on `dev.gitlive:firebase-app` produced an aggregated
  `build/kotlin/swiftPMDependenciesMetadataForLockFiles` listing `firebase-ios-sdk` / `FirebaseCore`
  / `12.17.0`.)
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
   to get a KMP framework into Xcode; for an app that consumes this SDK, direct integration is the
   recommended one:

   - ✅ **[Direct integration](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html)**
     — add the `embedAndSignAppleFrameworkForXcode` run-script build phase (KGP exposes the
     SwiftPM-aware `integrateEmbedAndSign` / `integrateLinkagePackage` tasks). On each build Gradle
     compiles, resolves the inherited `firebase-ios-sdk`, generates the synthetic Swift package, and
     embeds/signs the framework. Setup steps and the exact script are in that guide (and the
     [CocoaPods→SwiftPM migration guide](https://kotlinlang.org/docs/multiplatform/multiplatform-cocoapods-spm-migration.html));
     background on SwiftPM import is [here](https://kotlinlang.org/docs/multiplatform/multiplatform-spm-import.html).
   - ⚠️ **[Remote / SwiftPM export](https://kotlinlang.org/docs/multiplatform/multiplatform-spm-export.html)**
     (packaging your shared module as its own Swift package via an exported XCFramework +
     `Package.swift`) needs **Kotlin 2.4.20+** in your build. From that version Kotlin can export a
     module that *uses* SwiftPM import, and the generated `Package.swift` declares the inherited
     `firebase-ios-sdk` products ([KT-84420](https://youtrack.jetbrains.com/issue/KT-84420)). The
     feature is experimental, and the generated package refers to those dependencies by local path,
     so it can be added to Xcode as a local package but can't be published as a versioned remote
     package without editing the manifest. It has not been verified end to end with this SDK.
3. **Build your shared framework as static** — set `isStatic = true` on your `binaries.framework`.
   Firebase's SwiftPM products are static libraries; a **dynamic** shared framework produces
   `@rpath/…framework` load commands that aren't satisfied at runtime, so the app crashes with a
   `dyld: Library not loaded` error. `isStatic = true` embeds the symbols and defers Firebase linkage
   to the final Xcode link.

**Requirements:** Kotlin **2.4+** and Xcode **26.2+** (the SwiftPM import/integration toolchain).
For **Xcode 27**, use Kotlin **2.4.20+** in your build: earlier 2.4.x releases can't capture Xcode
27's link step ([KT-87196](https://youtrack.jetbrains.com/issue/KT-87196)). Kotlin 2.4.x is
officially tested up to Xcode 26.4; full Xcode 27 support is planned for Kotlin 2.5.0
([KT-87869](https://youtrack.jetbrains.com/issue/KT-87869)).

### Known gotchas

- **iOS/macOS/tvOS test tasks.** Kotlin/Native test tasks such as `iosSimulatorArm64Test` link the
  inherited `firebase-ios-sdk` themselves: the Kotlin Gradle plugin adds the SwiftPM linker
  arguments to test executables and sets the DYLD search paths when it runs them. This SDK's own CI
  runs its Apple tests this way. Tests that use FirebaseAuth or FirebaseInstallations on a simulator
  need a keychain entitlement linked into the test binary; see `enableKeychainForTests` in
  `firebase-auth/build.gradle.kts`. Resources bundled inside Swift packages aren't available to
  Kotlin/Native test runs yet ([KT-83877](https://youtrack.jetbrains.com/issue/KT-83877)).
- **Kotlin CocoaPods plugin, or Firebase added to Xcode by hand.** Because the SDK publishes its
  SwiftPM dependencies, the Kotlin Gradle plugin fails the CocoaPods plugin's `syncFramework`
  (in `checkSwiftPMDependencies`), and fails direct integration when the Xcode project doesn't
  include the generated linkage package ("SwiftPM linkage package not integrated into Xcode
  project"). This is by design. Both errors print the command that runs the `integrateEmbedAndSign`
  and `integrateLinkagePackage` tasks against your Xcode project; see Kotlin's
  [CocoaPods to SwiftPM migration guide](https://kotl.in/cocoapods-to-swiftpm-migration). Afterwards
  remove the Firebase pods from your Podfile (see "Don't mix package managers for Firebase" below).
- **Deployment target.** This SDK targets **iOS 15 / tvOS 15 / macOS 12**. iOS and tvOS match
  firebase-ios-sdk 12's own declared minimums (iOS 15 / macCatalyst 15 / macOS 10.15 / tvOS 15 /
  watchOS 7). macOS is 12 rather than Firebase's 10.15 because Kotlin/Native builds macOS code for
  macOS 12 and later. Your app's deployment target must be **≥** these. The iOS and tvOS floors rose
  from 13 in firebase-ios-sdk 12.0.0 — if your app still targets iOS 13 or 14, stay on a release of
  this SDK that pins `firebase-ios-sdk` 11.x.
- **"module.modulemap has been modified since the module file … was built" (Kotlin 2.4.20).**
  After the resolved `firebase-ios-sdk` version changes, for example when you upgrade
  `dev.gitlive:firebase-*`, Kotlin 2.4.20 can reuse stale Xcode build state and fail with this error
  ([KT-88106](https://youtrack.jetbrains.com/issue/KT-88106), fix planned for Kotlin 2.5.0). Delete
  `build/kotlin/swiftPMXcodeDumps` in your root project, or run `./gradlew clean`.
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
| FirebaseRemoteConfig  | `FirebaseRemoteConfig`, `FirebaseRemoteConfigInternal` |
| FirebaseCrashlytics   | `FirebaseCrashlytics`                  |
| FirebaseDatabase      | `FirebaseDatabase`, `FirebaseDatabaseInternal` |
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
