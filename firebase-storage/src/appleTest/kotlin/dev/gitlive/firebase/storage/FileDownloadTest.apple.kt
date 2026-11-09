package dev.gitlive.firebase.storage

import android.net.Uri
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL

actual fun temporaryFile(name: String): Uri = Uri.parse(requireNotNull(NSURL.fileURLWithPath(NSTemporaryDirectory() + name).absoluteString))

actual val supportsFileDownloads: Boolean = true

actual val supportsFileUploads: Boolean = true
