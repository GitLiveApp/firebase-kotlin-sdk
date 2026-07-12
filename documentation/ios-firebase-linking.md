# Linking the Firebase iOS SDK (SwiftPM)

The Firebase Kotlin SDK is distributed as **Maven Central klibs** and consumed by a
Kotlin Multiplatform (KMP) app, whose iOS framework is then added to an Xcode project. On iOS the
GitLive klibs call the Firebase Obj-C API through cinterop, but — as before — **the official
Firebase iOS SDK is not a transitive dependency**: it is not bundled in the klibs and does not flow
across the Maven boundary. The **downstream KMP app** is responsible for making `firebase-ios-sdk`
available when it links its own iOS framework.

This is unchanged in spirit from the old CocoaPods setup ("Firebase iOS frameworks must be linked
separately by consumers"); only the mechanism moves from CocoaPods to SwiftPM.

## Why the app, not this SDK, declares firebase-ios-sdk

`swiftPMDependencies` (how this SDK's build obtains Firebase for its own cinterop) is a **build-time
concern of the project that declares it**. It is not recorded in published Gradle module metadata,
so a Maven consumer of `dev.gitlive:firebase-*` does not inherit it. The Firebase native dependency
must therefore be declared at the **framework-producing boundary — the KMP app**.

## Recommended: declare firebase-ios-sdk in the app's shared module

In the downstream KMP app's shared module, mirror how this SDK consumes Firebase — declare only the
products the app actually uses:

```kotlin
kotlin {
    swiftPMDependencies {
        discoverClangModulesImplicitly = false
        iosMinimumDeploymentTarget.set("13.0")
        swiftPackage(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
            version = exact("11.8.0"), // keep in step with this SDK's firebase-ios-sdk version
            products = listOf(product("FirebaseAuth"), product("FirebaseFirestore")),
            importedClangModules = listOf("FirebaseAuthInternal", "FirebaseFirestoreInternal"),
        )
    }
}
```

Product → Clang module mapping (only needed when `discoverClangModulesImplicitly = false`; several
Firebase products expose their Obj-C APIs from an `*Internal` module):

| SPM product           | `importedClangModules`          |
|-----------------------|---------------------------------|
| FirebaseCore          | `FirebaseCore`                  |
| FirebaseAnalytics     | `FirebaseAnalytics`             |
| FirebaseAuth          | `FirebaseAuth`, `FirebaseAuthInternal` |
| FirebaseRemoteConfig  | `FirebaseRemoteConfigInternal`  |
| FirebaseCrashlytics   | `FirebaseCrashlytics`           |
| FirebaseDatabase      | `FirebaseDatabaseInternal`      |
| FirebaseFirestore     | `FirebaseFirestoreInternal`     |
| FirebaseFunctions     | `FirebaseFunctions`             |
| FirebaseInstallations | `FirebaseInstallations`         |
| FirebaseMessaging     | `FirebaseMessaging`             |
| FirebasePerformance   | `FirebasePerformance`           |
| FirebaseStorage       | `FirebaseStorage`               |

> Keep the app's `firebase-ios-sdk` version aligned with the one this SDK was built against
> (see `firebase-ios-sdk` in `gradle/libs.versions.toml`) to avoid Obj-C symbol/ABI drift.

## Distributing the app's framework to Xcode via SwiftPM

Kotlin's SwiftPM **export** of a module that itself uses `swiftPMDependencies` is not yet supported
(JetBrains [KT-84420](https://youtrack.jetbrains.com/issue/KT-84420)). So the app ships its shared
framework the normal way and provides Firebase to Xcode with a **hand-authored `Package.swift`**:

1. Assemble the app's shared **XCFramework**, linking its framework with
   `-undefined dynamic_lookup` so the Firebase Obj-C symbols are resolved at the final app link
   rather than bundled:
   ```kotlin
   iosTarget.binaries.framework {
       baseName = "Shared"
       linkerOpts("-undefined", "dynamic_lookup")
   }
   ```
2. Author a `Package.swift` that vendors that XCFramework and pulls the matching firebase-ios-sdk
   products, so Xcode resolves them transitively for the app — no manual "add Firebase" step:
   ```swift
   // swift-tools-version:5.9
   import PackageDescription

   let package = Package(
       name: "Shared",
       platforms: [.iOS(.v13)],
       products: [
           .library(name: "Shared", targets: ["Shared", "SharedFirebaseLink"]),
       ],
       dependencies: [
           .package(url: "https://github.com/firebase/firebase-ios-sdk.git", exact: "11.8.0"),
       ],
       targets: [
           .binaryTarget(name: "Shared", path: "build/XCFrameworks/release/Shared.xcframework"),
           // Empty target: exists only to pull the Firebase products the shared framework references.
           .target(
               name: "SharedFirebaseLink",
               dependencies: [
                   .product(name: "FirebaseAuth", package: "firebase-ios-sdk"),
                   .product(name: "FirebaseFirestore", package: "firebase-ios-sdk"),
               ],
               path: "spm/SharedFirebaseLink" // contains a single empty .swift file
           ),
       ]
   )
   ```
   For a tagged release, switch `.binaryTarget(path:)` to `.binaryTarget(url:checksum:)`.

The Xcode app then just depends on the `Shared` product and `import Shared` — Firebase is linked
automatically via the transitive `firebase-ios-sdk` dependency.

## Notes

- The GitLive SDK itself no longer ships a `Package.swift` or podspecs; it is Maven-only, and the
  Firebase linkage is owned by the app per the above.
- Kotlin 2.4.0+ is required (the `swiftPMDependencies` DSL). Building against firebase-ios-sdk pulls
  its full transitive graph (gRPC/abseil/leveldb/BoringSSL/nanopb for Firestore), which is large —
  budget disk/time on CI accordingly.
