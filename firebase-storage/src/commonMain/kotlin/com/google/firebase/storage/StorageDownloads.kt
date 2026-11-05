/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

/*
 * Downloads to a file or a stream: the FileDownloadTask and StreamDownloadTask of the Android SDK. The members taking the file (StorageReference.getFile,
 * activeDownloadTasks, FirebaseStorage.maxDownloadRetryTimeMillis) are declared on their classes. The JS SDK has no file
 * downloads (it downloads through URLs): there the task fails at once, the active downloads are empty and the retry time
 * has no effect, behind the UnsupportedOnJs opt-in.
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

/**
 * A download read through a stream, as the Android SDK's `StreamDownloadTask`. The Apple and JS SDKs download into
 * memory, so off Android and the JVM the whole object is downloaded first and [TaskSnapshot.stream] reads the bytes;
 * [StreamProcessor] runs once they are there.
 */
public expect abstract class StreamDownloadTask : StorageTask<StreamDownloadTask.TaskSnapshot> {
    /** Reads the downloaded stream; the task succeeds when it returns and fails with what it throws. */
    public fun interface StreamProcessor {
        public fun doInBackground(state: TaskSnapshot, stream: java.io.InputStream)
    }

    /** The state of a download. */
    public inner class TaskSnapshot : StorageTask<TaskSnapshot>.SnapshotBase {
        /** The bytes downloaded so far. */
        public val bytesTransferred: Long

        /** The total size of the download. */
        public val totalByteCount: Long

        /** The downloaded bytes. */
        public val stream: java.io.InputStream
    }
}
