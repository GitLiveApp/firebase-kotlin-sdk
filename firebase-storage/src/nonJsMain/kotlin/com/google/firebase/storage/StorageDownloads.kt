/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

/*
 * Downloads to a file, which the JS SDK does not have (it downloads through URLs). The Android SDK takes an
 * android.net.Uri or java.io.File, which common code cannot name, so getFile takes the platform's file type as Any:
 * an android.net.Uri or java.io.File on Android and the JVM, an NSURL on Apple platforms, or a String path everywhere.
 * On Android the SDK's members bind for Android code (a member wins over an extension) and this file is shipped.
 */

/** A download to a file, as the Android SDK's `FileDownloadTask`; [TaskSnapshot] reports its progress. */
public expect abstract class FileDownloadTask : StorageTask<FileDownloadTask.TaskSnapshot> {
    /** The state of a download. */
    public inner class TaskSnapshot : StorageTask<TaskSnapshot>.SnapshotBase {
        /** The bytes downloaded so far. */
        public val bytesTransferred: Long

        /** The total size of the download. */
        public val totalByteCount: Long
    }
}

/** Downloads the object to the file [destination] (see the platform file types above). */
public expect fun StorageReference.getFile(destination: Any): FileDownloadTask

/** The downloads from this reference that have not completed. */
public expect val StorageReference.activeDownloadTasks: List<FileDownloadTask>

/** The maximum time to retry downloads, in milliseconds. */
public expect var FirebaseStorage.maxDownloadRetryTimeMillis: Long
