/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.Runnable

/**
 * The entry point of Cloud Firestore, mirroring `com.google.firebase.firestore.FirebaseFirestore` from the Firebase
 * Android SDK: obtained per [FirebaseApp] and database with the static accessors (or the `Firebase.firestore`
 * extensions), it creates references, batches and transactions and manages the network and the local cache.
 *
 * Not mirrored: the pipeline API and `loadBundle` from JVM streams (see `api/android-sdk/exclusions.txt`).
 */
public expect class FirebaseFirestore {
    /** The [FirebaseApp] this instance belongs to. */
    public val app: FirebaseApp

    /** The settings; can only be set before the instance is used. */
    public var firestoreSettings: FirebaseFirestoreSettings

    /** The manager of the persistent cache's indexes, or null when persistence is disabled. */
    public val persistentCacheIndexManager: PersistentCacheIndexManager?

    /** A [CollectionReference] for the collection at [collectionPath]. */
    public fun collection(collectionPath: String): CollectionReference

    /** A [Query] over every collection named [collectionId]. */
    public fun collectionGroup(collectionId: String): Query

    /** A [DocumentReference] for the document at [documentPath]. */
    public fun document(documentPath: String): DocumentReference

    /** A new [WriteBatch]. */
    public fun batch(): WriteBatch

    /** Runs [batchFunction] on a new [WriteBatch] and commits it. */
    public fun runBatch(batchFunction: WriteBatch.Function): Task<Nothing?>

    /** Runs [updateFunction] in a transaction, retrying it on contention, and completes with its result. */
    public fun <TResult> runTransaction(updateFunction: Transaction.Function<TResult>): Task<TResult>

    /** Runs [updateFunction] in a transaction with [options] and completes with its result. */
    public fun <TResult> runTransaction(options: TransactionOptions, updateFunction: Transaction.Function<TResult>): Task<TResult>

    /** Registers [runnable] to be called whenever all the active snapshot listeners are in sync. */
    public fun addSnapshotsInSyncListener(runnable: Runnable): ListenerRegistration

    /** Loads a Firestore bundle into the local cache. */
    public fun loadBundle(bundleData: ByteArray): LoadBundleTask

    /** The query named [name] in a loaded bundle, or null. */
    public fun getNamedQuery(name: String): Task<Query?>

    /** Connects to the Firestore emulator at [host]:[port]; must be called before the instance is used. */
    public fun useEmulator(host: String, port: Int)

    /** Clears the persistent cache; only allowed while the instance is not started or is terminated. */
    public fun clearPersistence(): Task<Nothing?>

    /** Disables network access; reads are served from the cache and writes are queued. */
    public fun disableNetwork(): Task<Nothing?>

    /** Re-enables network access after [disableNetwork]. */
    public fun enableNetwork(): Task<Nothing?>

    /** Terminates the instance; further use fails and a new instance is returned by the accessors. */
    public fun terminate(): Task<Nothing?>

    /** Completes when all the pending writes have been acknowledged by the backend. */
    public fun waitForPendingWrites(): Task<Nothing?>

    public companion object {
        /** The instance of the default [FirebaseApp] and the default database. */
        public fun getInstance(): FirebaseFirestore

        /** The instance of [app] and the default database. */
        public fun getInstance(app: FirebaseApp): FirebaseFirestore

        /** The instance of [app] and the named [database]. */
        public fun getInstance(app: FirebaseApp, database: String): FirebaseFirestore

        /** The instance of the default [FirebaseApp] and the named [database]. */
        public fun getInstance(database: String): FirebaseFirestore

        /** Enables or disables the SDK's debug logging. */
        public fun setLoggingEnabled(loggingEnabled: Boolean)
    }
}

/** Manages the indexes of the persistent cache, mirroring the Android SDK's `PersistentCacheIndexManager`. */
public expect class PersistentCacheIndexManager {
    /** Lets the SDK create cache indexes automatically for queries with poor cache performance. */
    public fun enableIndexAutoCreation()

    /** Stops the automatic creation of cache indexes; existing indexes are kept. */
    public fun disableIndexAutoCreation()

    /** Deletes every cache index. */
    public fun deleteAllIndexes()
}

/**
 * The loading of a bundle, mirroring the Android SDK's `LoadBundleTask`: a [Task] of the final [LoadBundleTaskProgress]
 * that also reports intermediate progress to [addOnProgressListener]. Only created by `FirebaseFirestore.loadBundle`.
 */
public expect abstract class LoadBundleTask : Task<LoadBundleTaskProgress> {
    /** Registers [listener] for the progress of the loading; it is also called with the final progress. */
    public fun addOnProgressListener(listener: OnProgressListener<LoadBundleTaskProgress>): LoadBundleTask
}

/** The progress of a [LoadBundleTask]. */
public expect class LoadBundleTaskProgress {
    /** The number of documents loaded so far. */
    public val documentsLoaded: Int

    /** The number of documents in the bundle. */
    public val totalDocuments: Int

    /** The number of bytes loaded so far. */
    public val bytesLoaded: Long

    /** The number of bytes in the bundle. */
    public val totalBytes: Long

    /** Whether the loading is running, succeeded or failed. */
    public val taskState: TaskState

    /** The failure when [taskState] is [TaskState.ERROR]. */
    public val exception: Exception?

    public enum class TaskState {
        ERROR,
        RUNNING,
        SUCCESS,
    }
}
