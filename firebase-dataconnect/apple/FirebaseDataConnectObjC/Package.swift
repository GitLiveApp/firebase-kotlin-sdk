// swift-tools-version:5.9

// Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.

// The Firebase Data Connect iOS SDK is Swift-only, which Kotlin/Native cannot import. This package wraps it in an
// Objective-C API (see Sources), the workaround JetBrains recommends for Swift-only packages with the Kotlin SwiftPM
// import; firebase-dataconnect/build.gradle.kts imports it with localSwiftPackage(). firebase-ios-sdk is a dependency of
// the Data Connect SDK, so the Kotlin SwiftPM lock file keeps it at the version the other modules use.

import PackageDescription

let package = Package(
  name: "FirebaseDataConnectObjC",
  platforms: [.iOS(.v15), .macOS(.v12), .tvOS(.v15)],
  products: [
    .library(name: "FirebaseDataConnectObjC", targets: ["FirebaseDataConnectObjC"]),
  ],
  dependencies: [
    .package(url: "https://github.com/firebase/data-connect-ios-sdk.git", from: "11.12.6"),
    // FirebaseCore for FirebaseApp.app(name:); the version is the firebase-ios-sdk version of gradle/libs.versions.toml.
    .package(url: "https://github.com/firebase/firebase-ios-sdk.git", from: "12.17.0"),
  ],
  targets: [
    .target(
      name: "FirebaseDataConnectObjC",
      dependencies: [
        .product(name: "FirebaseDataConnect", package: "data-connect-ios-sdk"),
        .product(name: "FirebaseCore", package: "firebase-ios-sdk"),
      ],
      path: "Sources"
    ),
  ]
)
