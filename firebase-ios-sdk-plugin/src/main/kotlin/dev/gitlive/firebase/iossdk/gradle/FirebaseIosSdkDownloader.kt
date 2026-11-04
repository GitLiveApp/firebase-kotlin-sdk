package dev.gitlive.firebase.iossdk.gradle

import org.gradle.api.logging.Logging
import java.io.File
import java.net.URI
import java.security.MessageDigest

/**
 * Downloads the official Firebase iOS SDK release zip (the "manual installation" distribution Firebase still
 * publishes alongside Swift Package Manager, now that CocoaPods no longer carries 13.x), verifies it against a
 * pinned SHA-256 before trusting it, and extracts it.
 *
 * Runs synchronously during Gradle configuration rather than as a lazy task: every module needs the real,
 * already-extracted directory contents available *during configuration* to discover a framework's transitive
 * sibling frameworks (see [FirebaseIosSdkExtension]), which a lazy task's output can't safely guarantee hasn't
 * run yet. The ~388MB download/extract only happens once per machine per version - every call after the first
 * (whether from another module in the same build, or a future build entirely) is a handful of up-to-date
 * filesystem checks, keyed by the version-qualified cache directory itself rather than any in-memory state.
 */
internal object FirebaseIosSdkDownloader {

    private val logger = Logging.getLogger(FirebaseIosSdkDownloader::class.java)

    fun ensureExtracted(gradleUserHomeDir: File, version: String, sha256: String): File {
        val expectedSha256 = sha256.substringAfter(":").lowercase()
        val cacheDir = File(gradleUserHomeDir, "caches/firebase-ios-sdk-plugin/$version")
        val extractedDir = File(cacheDir, "extracted")
        val completionMarker = File(cacheDir, "extracted.sha256")

        if (extractedDir.isDirectory && completionMarker.readTextOrNull() == expectedSha256) {
            return extractedDir
        }

        cacheDir.mkdirs()
        val zipFile = File(cacheDir, "Firebase.zip")
        if (!zipFile.exists() || zipFile.sha256() != expectedSha256) {
            val url = "https://github.com/firebase/firebase-ios-sdk/releases/download/$version/Firebase.zip"
            logger.lifecycle("Downloading Firebase iOS SDK $version from $url ...")
            val partial = File(cacheDir, "${zipFile.name}.part")
            URI(url).toURL().openStream().use { input -> partial.outputStream().use { input.copyTo(it) } }

            val actualSha256 = partial.sha256()
            if (actualSha256 != expectedSha256) {
                partial.delete()
                error(
                    "Downloaded Firebase.zip for $version has SHA-256 $actualSha256 but $expectedSha256 was " +
                        "pinned. Refusing to use a file that doesn't match the expected checksum - either the " +
                        "pinned checksum is stale (Firebase re-cut this release) or the download was corrupted " +
                        "or tampered with in transit.",
                )
            }
            zipFile.delete()
            check(partial.renameTo(zipFile)) { "Failed to move $partial to $zipFile" }
        }

        if (extractedDir.exists()) extractedDir.deleteRecursively()
        extractedDir.mkdirs()
        logger.lifecycle("Extracting Firebase iOS SDK $version ...")
        // `ditto` (not java.util.zip) because the zip contains real symlinks (the classic macOS/Catalyst
        // framework bundle layout has Headers/Modules/Versions-Current as symlinks) that plain JVM zip
        // extraction would flatten into regular files containing the link target as text, silently breaking
        // every macOS/maccatalyst framework slice.
        val process = ProcessBuilder("ditto", "-x", "-k", zipFile.absolutePath, extractedDir.absolutePath)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { "ditto failed to extract $zipFile into $extractedDir:\n$output" }

        completionMarker.writeText(expectedSha256)
        return extractedDir
    }
}

private fun File.readTextOrNull(): String? = if (isFile) readText().trim() else null

private fun File.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream().use { input ->
        val buffer = ByteArray(1 shl 20)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}
