package dev.gitlive.firebase.iossdk.gradle

/**
 * The XCFramework slice (`LibraryIdentifier` in an `Info.plist`) that a given Kotlin/Native Apple target needs.
 * Hardcoded rather than read from each framework's `Info.plist` at configuration time, so wiring a framework
 * doesn't require the zip to already be downloaded/extracted - the path these resolve to is only required to
 * exist once the dependent cinterop/link task actually runs, by which point [FirebaseIosSdkDownloader] has.
 * Verified directly against the real `13.1.1` release zip's `Info.plist` files.
 */
internal enum class AppleSlice(val libraryIdentifier: String) {
    IosDevice("ios-arm64"),
    IosSimulator("ios-arm64_x86_64-simulator"),
    TvosDevice("tvos-arm64"),
    TvosSimulator("tvos-arm64_x86_64-simulator"),
    MacosUniversal("macos-arm64_x86_64"),
}

/** Returns `null` for Kotlin/Native targets this plugin has nothing to offer (e.g. linuxX64, mingwX64). */
internal fun appleSliceForTarget(targetName: String): AppleSlice? = when (targetName) {
    "iosArm64" -> AppleSlice.IosDevice
    "iosX64", "iosSimulatorArm64" -> AppleSlice.IosSimulator
    "tvosArm64" -> AppleSlice.TvosDevice
    "tvosX64", "tvosSimulatorArm64" -> AppleSlice.TvosSimulator
    "macosArm64", "macosX64" -> AppleSlice.MacosUniversal
    else -> null
}
