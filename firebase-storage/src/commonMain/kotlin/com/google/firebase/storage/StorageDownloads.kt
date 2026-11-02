/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

/*
 * Downloads to a file: the FileDownloadTask of the Android SDK. The members taking the file (StorageReference.getFile,
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
