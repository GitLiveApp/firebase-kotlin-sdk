package dev.gitlive.firebase.iossdk.gradle

/**
 * Several Firebase frameworks (FirebaseCoreInternal's heartbeat logging, FirebaseSessions, etc.) are written in
 * Swift. Xcode's own build system auto-links the Swift compatibility/back-deployment libraries (e.g.
 * `swiftCompatibility56`) those binaries need; Kotlin/Native's linker invocation calls `ld` directly and knows
 * nothing about them, so linking fails with "symbol(s) not found" for `_swift_FORCE_LOAD_$_swiftCompatibility56`
 * and similar. `swiftc -print-target-info` is the toolchain's own, version-independent way to ask "where do
 * these live for this target" rather than hardcoding an Xcode-version-specific path.
 */
internal fun swiftRuntimeLibrarySearchPaths(targetTriple: String): List<String> {
    val process = ProcessBuilder("xcrun", "swiftc", "-print-target-info", "-target", targetTriple)
        .redirectErrorStream(false)
        .start()
    val output = process.inputStream.bufferedReader().readText()
    process.waitFor()
    // Minimal extraction rather than a full JSON parser: pull the quoted strings out of the
    // "runtimeLibraryPaths": [ ... ] array, which is all this trusted, local tool-generated output contains.
    val arrayText = Regex("\"runtimeLibraryPaths\"\\s*:\\s*\\[(.*?)]", RegexOption.DOT_MATCHES_ALL)
        .find(output)
        ?.groupValues
        ?.get(1)
        .orEmpty()
    return Regex("\"([^\"]+)\"").findAll(arrayText).map { it.groupValues[1] }.toList()
}

/** The `swiftc -target` triple a Kotlin/Native Apple target's binaries are built for. */
internal fun swiftTargetTriple(targetName: String): String? = when (targetName) {
    "iosArm64" -> "arm64-apple-ios15.0"
    "iosX64" -> "x86_64-apple-ios15.0-simulator"
    "iosSimulatorArm64" -> "arm64-apple-ios15.0-simulator"
    "tvosArm64" -> "arm64-apple-tvos15.0"
    "tvosX64" -> "x86_64-apple-tvos15.0-simulator"
    "tvosSimulatorArm64" -> "arm64-apple-tvos15.0-simulator"
    "macosArm64" -> "arm64-apple-macos12.0"
    "macosX64" -> "x86_64-apple-macos12.0"
    else -> null
}
