package dev.gitlive.firebase.storage

import android.net.Uri

actual fun temporaryFile(name: String): Uri = Uri.parse(java.io.File.createTempFile(name, null).toURI().toString())

actual val supportsFileDownloads: Boolean = true

// The Android SDK's putFile(Uri) opens the file through ContentResolver.openFileDescriptor, which firebase-java-sdk does not implement.
actual val supportsFileUploads: Boolean = false
