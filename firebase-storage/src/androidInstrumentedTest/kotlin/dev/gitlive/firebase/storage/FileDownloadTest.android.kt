package dev.gitlive.firebase.storage

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry

actual fun temporaryFile(name: String): Uri = Uri.fromFile(java.io.File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, name))

actual val supportsFileDownloads: Boolean = true
