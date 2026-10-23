# Migrating from CocoaPods to SwiftPM import

The Firebase Kotlin SDK gets the [Firebase Apple SDK](https://github.com/firebase/firebase-ios-sdk) through the Kotlin
Gradle Plugin's [SwiftPM import](https://kotl.in/cocoapods-to-swiftpm-migration) instead of CocoaPods. Firebase stops
publishing new versions to CocoaPods after Firebase 12
([Firebase announcement](https://firebase.google.com/docs/ios/cocoapods-deprecation)), and CocoaPods trunk becomes
read-only on December 2, 2026 ([CocoaPods announcement](https://blog.cocoapods.org/CocoaPods-Specs-Repo/)).

This guide is for apps that used the SDK together with the Kotlin CocoaPods plugin (`kotlin("native.cocoapods")`).

## What changes for your project

- Each SDK module declares the Firebase Swift packages it wraps, and they are published with the module. Your project
  gets them as transitive dependencies, so you no longer add Firebase pods for the SDK modules you use.
- You need Kotlin 2.4 or later and Xcode 26.2 or later. The minimum deployment targets are iOS 15, tvOS 15 and macOS 12.
- The Kotlin CocoaPods plugin can't be combined with SwiftPM dependencies, including transitive ones. A project that
  still applies it fails with *"You are using CocoaPods integration with SwiftPM dependencies"*, followed by the command
  that switches your Xcode project to SwiftPM import (step 4 below).
- The `ios` extension properties return the same Objective-C classes, but from new Kotlin packages (step 6).

## Step by step

### 1. Update Kotlin and the SDK

Update the Kotlin Gradle Plugin to 2.4 or later and the `dev.gitlive` dependencies to the SwiftPM-based release.

### 2. Raise the deployment targets

Set at least iOS 15, tvOS 15 and macOS 12 in your Xcode project. If your own module declares SwiftPM dependencies,
set the same values in `swiftPMDependencies {}`:

```kotlin
kotlin {
    swiftPMDependencies {
        iosMinimumDeploymentTarget = "15.0"
    }
}
```

### 3. Remove CocoaPods from the Gradle module

In the module that builds the framework for your app, remove the Firebase `pod(...)` entries. They are no longer needed,
because the SDK modules bring their packages along.

If the `cocoapods {}` block has nothing else left, remove it together with the `kotlin("native.cocoapods")` plugin, and
move the framework configuration to the targets. A static framework is recommended with SwiftPM import:

```kotlin
// Before
kotlin {
    cocoapods {
        ios.deploymentTarget = "13.0"
        framework {
            baseName = "Shared"
        }
        pod("FirebaseCore")
        pod("FirebaseFirestore")
    }
}

// After
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
}
```

Other pods your module uses must move to `swiftPMDependencies {}` as well, because the plugin rejects any mix of the two.
See the [Kotlin migration guide](https://kotl.in/cocoapods-to-swiftpm-migration) for the DSL. `swiftPackage(...)` is
experimental, so a build script that calls it needs `@OptIn(ExperimentalKotlinGradlePluginApi::class)`.

### 4. Switch the Xcode project to SwiftPM import

Run this once, with the paths of your Xcode project and Kotlin module. The CocoaPods error from the list above prints
the same command with your paths filled in.

```shell
XCODEPROJ_PATH='iosApp/iosApp.xcodeproj' GRADLE_PROJECT_PATH=':shared' ./gradlew :shared:integrateEmbedAndSign :shared:integrateLinkagePackage
```

It adds a build phase that runs `embedAndSignAppleFrameworkForXcode` and a local Swift package,
`KotlinMultiplatformLinkedPackage`, that links the Firebase packages into your app. Commit the generated package.

The build phase runs Gradle, so turn off User Script Sandboxing (`ENABLE_USER_SCRIPT_SANDBOXING = NO`) for the app
target if Xcode has it on.

### 5. Remove CocoaPods from the Xcode project

If CocoaPods only provided Firebase and your Kotlin framework, remove it from the iOS project:

```shell
cd iosApp
pod deintegrate
rm -rf Podfile Podfile.lock Pods iosApp.xcworkspace
```

From now on, open the `.xcodeproj` instead of the `.xcworkspace`. If you still need CocoaPods for other libraries,
remove only the Kotlin framework pod from the `Podfile` and run `pod install` again.

### 6. Update imports of Firebase Apple SDK classes

Code that uses the `ios` properties or calls the Firebase Apple SDK through this SDK imports the Objective-C classes from
new packages. Replace the `cocoapods.<Pod>.` prefix with the package of the SDK module:

| SDK module               | CocoaPods package                     | SwiftPM import package                         |
|--------------------------|---------------------------------------|------------------------------------------------|
| `firebase-analytics`     | `cocoapods.FirebaseAnalytics`         | `swiftPMImport.dev.gitlive.firebase.analytics`     |
| `firebase-app`           | `cocoapods.FirebaseCore`              | `swiftPMImport.dev.gitlive.firebase.app`           |
| `firebase-auth`          | `cocoapods.FirebaseAuth`              | `swiftPMImport.dev.gitlive.firebase.auth`          |
| `firebase-config`        | `cocoapods.FirebaseRemoteConfig`      | `swiftPMImport.dev.gitlive.firebase.config`        |
| `firebase-crashlytics`   | `cocoapods.FirebaseCrashlytics`       | `swiftPMImport.dev.gitlive.firebase.crashlytics`   |
| `firebase-database`      | `cocoapods.FirebaseDatabase`          | `swiftPMImport.dev.gitlive.firebase.database`      |
| `firebase-firestore`     | `cocoapods.FirebaseFirestoreInternal` | `swiftPMImport.dev.gitlive.firebase.firestore`     |
| `firebase-functions`     | `cocoapods.FirebaseFunctions`         | `swiftPMImport.dev.gitlive.firebase.functions`     |
| `firebase-installations` | `cocoapods.FirebaseInstallations`     | `swiftPMImport.dev.gitlive.firebase.installations` |
| `firebase-messaging`     | `cocoapods.FirebaseMessaging`         | `swiftPMImport.dev.gitlive.firebase.messaging`     |
| `firebase-perf`          | `cocoapods.FirebasePerformance`       | `swiftPMImport.dev.gitlive.firebase.perf`          |
| `firebase-storage`       | `cocoapods.FirebaseStorage`           | `swiftPMImport.dev.gitlive.firebase.storage`       |

For example:

```kotlin
// Before
import cocoapods.FirebaseFirestoreInternal.FIRFirestore

// After
import swiftPMImport.dev.gitlive.firebase.firestore.FIRFirestore
```

FirebaseCore classes such as `FIRApp` always come from `firebase-app`'s package, whichever module uses them. Casts to the
forward declaration `objcnames.classes.FIRApp` are no longer needed: pass `FirebaseApp.ios` directly.

### 7. Update the Crashlytics dSYM upload script

If your app uploads dSYMs for Crashlytics, the script moves from the `Pods` directory to the Swift package checkout.
Change the run script phase to:

```shell
"${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"
```

## Firebase APIs removed in Firebase 12 and 13

The SDK builds against Firebase 13, which no longer has two values that the common API still accepts:

- `FirebaseOptions.gaTrackingId` is always `null` on Apple platforms, and the builder ignores it.
- `ActionCodeSettings.dynamicLinkDomain` is ignored on Apple platforms. Use `linkDomain` instead.

## After changing dependencies

When Gradle changes the SwiftPM dependencies, the next Xcode build regenerates `KotlinMultiplatformLinkedPackage` and
fails with *"Synthetic linkage package files changed during the build"* and *"Please go to File -> Package -> Resolve
Package Versions in Xcode"*. Resolve the packages or build again, then commit the regenerated files.
