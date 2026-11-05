/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import android.content.Context
import dev.gitlive.firebase.firestore.LOAD_BUNDLE_BUFFER_ANDROID_ONLY
import swiftPMImport.dev.gitlive.firebase.firestore.FIRFirestore
import swiftPMImport.dev.gitlive.firebase.firestore.FIRFirestoreSettings
import swiftPMImport.dev.gitlive.firebase.firestore.FIRLoadBundleTaskProgress
import swiftPMImport.dev.gitlive.firebase.firestore.FIRLoadBundleTaskState
import swiftPMImport.dev.gitlive.firebase.firestore.FIRMemoryCacheSettings
import swiftPMImport.dev.gitlive.firebase.firestore.FIRMemoryEagerGCSettings
import swiftPMImport.dev.gitlive.firebase.firestore.FIRMemoryLRUGCSettings
import swiftPMImport.dev.gitlive.firebase.firestore.FIRPersistentCacheIndexManager
import swiftPMImport.dev.gitlive.firebase.firestore.FIRPersistentCacheSettings
import swiftPMImport.dev.gitlive.firebase.firestore.FIRTransaction
import swiftPMImport.dev.gitlive.firebase.firestore.FIRTransactionOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import kotlinx.coroutines.Runnable
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.numberWithLong
import platform.darwin.dispatch_queue_t
import platform.posix.uint8_tVar
import platform.posix.memcpy
import platform.darwin.NSUIntegerVar
import platform.darwin.NSUInteger
import platform.darwin.NSInteger
import platform.Foundation.NSStreamStatusOpen
import platform.Foundation.NSStreamStatusNotOpen
import platform.Foundation.NSStreamStatusError
import platform.Foundation.NSStreamStatusClosed
import platform.Foundation.NSStreamStatusAtEnd
import platform.Foundation.NSStreamPropertyKey
import platform.Foundation.NSRunLoopMode
import platform.Foundation.NSRunLoop
import platform.Foundation.NSInputStream
import platform.Foundation.NSData
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.convert
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.CPointerVar

/** @property ios The underlying Firebase iOS SDK object. */
public actual class FirebaseFirestore internal constructor(public val ios: FIRFirestore) {
    public actual val app: FirebaseApp
        get() = FirebaseApp.getApps(Context()).firstOrNull { (it.ios as Any?) == ios.app } ?: FirebaseApp.getInstance()

    /** The iOS SDK does not expose the cache settings it was given, so the settings set through the layer are kept per instance. */
    public actual var firestoreSettings: FirebaseFirestoreSettings
        get() = Settings.byInstance[ios] ?: FirebaseFirestoreSettings.Builder().build().also { Settings.byInstance[ios] = it }
        set(value) {
            ios.settings = value.toIos(ios.settings)
            Settings.byInstance[ios] = value
        }

    public actual val persistentCacheIndexManager: PersistentCacheIndexManager?
        get() = ios.persistentCacheIndexManager?.let { PersistentCacheIndexManager(it) }

    public actual fun collection(collectionPath: String): CollectionReference = CollectionReference(ios.collectionWithPath(collectionPath))

    public actual fun collectionGroup(collectionId: String): Query = Query(ios.collectionGroupWithID(collectionId))

    public actual fun document(documentPath: String): DocumentReference = DocumentReference(ios.documentWithPath(documentPath))

    public actual fun batch(): WriteBatch = WriteBatch(ios.batch())

    public actual fun runBatch(batchFunction: WriteBatch.Function): Task<Nothing?> {
        val batch = batch()
        try {
            batchFunction.apply(batch)
        } catch (e: Exception) {
            return TaskCompletionSource<Nothing?>().apply { setException(e) }.task
        }
        return batch.commit()
    }

    public actual fun <TResult> runTransaction(updateFunction: Transaction.Function<TResult>): Task<TResult> = runBlockTransaction(null) { updateFunction.apply(it) }

    public actual fun <TResult> runTransaction(options: TransactionOptions, updateFunction: Transaction.Function<TResult>): Task<TResult> = runBlockTransaction(options) { updateFunction.apply(it) }

    /**
     * Runs [updateFunction] in a transaction. A Kotlin exception thrown by it must not cross the SDK's block, so it is
     * kept aside, reported to the SDK as an error (which aborts the transaction) and rethrown from the completion.
     */
    internal fun <TResult> runBlockTransaction(options: TransactionOptions?, updateFunction: (Transaction) -> TResult): Task<TResult> {
        val source = TaskCompletionSource<TResult>()
        var failure: Throwable? = null
        val block: (FIRTransaction?, CPointer<ObjCObjectVar<NSError?>>?) -> Any? = { transaction, errorPointer ->
            try {
                updateFunction(Transaction(transaction!!))
            } catch (e: Throwable) {
                failure = e
                errorPointer?.pointed?.value = e.toNSError()
                null
            }
        }
        val completion: (Any?, NSError?) -> Unit = { result, error ->
            @Suppress("UNCHECKED_CAST")
            when {
                failure != null -> source.setException(failure as? Exception ?: RuntimeException(failure))
                error != null -> source.setException(error.toFirestoreException())
                else -> source.setResult(result as TResult)
            }
        }
        if (options == null) {
            ios.runTransactionWithBlock(block, completion)
        } else {
            ios.runTransactionWithOptions(FIRTransactionOptions().apply { maxAttempts = options.maxAttempts.toLong() }, block, completion)
        }
        return source.task
    }

    public actual fun addSnapshotsInSyncListener(runnable: Runnable): ListenerRegistration {
        val registration = ios.addSnapshotsInSyncListener { runnable.run() }
        return ListenerRegistration { registration.remove() }
    }

    public actual fun loadBundle(bundleData: ByteArray): LoadBundleTask {
        val task = LoadBundleTaskImpl()
        ios.loadBundle(bundleData.toNSData()).addObserver { progress -> task.update(progress!!.toCompat()) }
        return task
    }

    /** The stream is read by the iOS SDK's `loadBundleStream:` through an `NSInputStream` over it (see [InputStreamAdapter]). */
    public actual fun loadBundle(bundleData: java.io.InputStream): LoadBundleTask {
        val task = LoadBundleTaskImpl()
        ios.loadBundleStream(InputStreamAdapter(bundleData)).addObserver { progress -> task.update(progress!!.toCompat()) }
        return task
    }

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(LOAD_BUNDLE_BUFFER_ANDROID_ONLY, ReplaceWith("loadBundle(bundleData.array())"), DeprecationLevel.ERROR)
    public actual fun loadBundle(bundleData: java.nio.ByteBuffer): LoadBundleTask = throw UnsupportedOperationException(LOAD_BUNDLE_BUFFER_ANDROID_ONLY)

    public actual fun getNamedQuery(name: String): Task<Query?> = task { completion -> ios.getQueryNamed(name) { query -> completion(query?.let { Query(it) }, null) } }

    public actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host, port.toLong())
        ios.settings = ios.settings.apply { sslEnabled = false }
    }

    public actual fun clearPersistence(): Task<Nothing?> = write { ios.clearPersistenceWithCompletion(it) }

    public actual fun disableNetwork(): Task<Nothing?> = write { ios.disableNetworkWithCompletion(it) }

    public actual fun enableNetwork(): Task<Nothing?> = write { ios.enableNetworkWithCompletion(it) }

    public actual fun terminate(): Task<Nothing?> {
        Settings.byInstance.remove(ios)
        return write { ios.terminateWithCompletion(it) }
    }

    public actual fun waitForPendingWrites(): Task<Nothing?> = write { ios.waitForPendingWritesWithCompletion(it) }

    override fun equals(other: Any?): Boolean = other is FirebaseFirestore && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = ios.description ?: "FirebaseFirestore"

    private object Settings {
        val byInstance = mutableMapOf<FIRFirestore, FirebaseFirestoreSettings>()
    }

    public actual companion object {
        public actual fun getInstance(): FirebaseFirestore = FirebaseFirestore(FIRFirestore.firestore())

        public actual fun getInstance(app: FirebaseApp): FirebaseFirestore = FirebaseFirestore(FIRFirestore.firestoreForApp(app.ios as objcnames.classes.FIRApp))

        public actual fun getInstance(app: FirebaseApp, database: String): FirebaseFirestore = FirebaseFirestore(FIRFirestore.firestoreForApp(app.ios as objcnames.classes.FIRApp, database))

        public actual fun getInstance(database: String): FirebaseFirestore = FirebaseFirestore(FIRFirestore.firestoreForDatabase(database))

        public actual fun setLoggingEnabled(loggingEnabled: Boolean) {
            FIRFirestore.enableLogging(loggingEnabled)
        }
    }
}

/** [this] applied to [current], the SDK's settings; the dispatch queue the dev.gitlive layer may have configured is kept. */
private fun FirebaseFirestoreSettings.toIos(current: FIRFirestoreSettings): FIRFirestoreSettings = FIRFirestoreSettings().also { settings ->
    settings.host = host
    settings.sslEnabled = isSslEnabled
    when (val cache = cacheSettings) {
        is PersistentCacheSettings -> settings.cacheSettings = FIRPersistentCacheSettings(NSNumber.numberWithLong(cache.sizeBytes))
        is MemoryCacheSettings -> settings.cacheSettings = FIRMemoryCacheSettings(
            when (val gc = cache.garbageCollectorSettings) {
                is MemoryLruGcSettings -> FIRMemoryLRUGCSettings(NSNumber.numberWithLong(gc.sizeBytes))
                else -> FIRMemoryEagerGCSettings()
            },
        )
        else -> settings.cacheSettings = current.cacheSettings
    }
    @Suppress("UNCHECKED_CAST")
    settings.dispatchQueue = (callbackContext as? dispatch_queue_t) ?: current.dispatchQueue
}

private fun FIRLoadBundleTaskProgress.toCompat(): LoadBundleTaskProgress = LoadBundleTaskProgress(
    documentsLoaded.toInt(),
    totalDocuments.toInt(),
    bytesLoaded,
    totalBytes,
    when (state) {
        FIRLoadBundleTaskState.FIRLoadBundleTaskStateSuccess -> LoadBundleTaskProgress.TaskState.SUCCESS
        FIRLoadBundleTaskState.FIRLoadBundleTaskStateError -> LoadBundleTaskProgress.TaskState.ERROR
        else -> LoadBundleTaskProgress.TaskState.RUNNING
    },
    null,
)

/** @property ios The underlying Firebase iOS SDK object. */
public actual class PersistentCacheIndexManager internal constructor(public val ios: FIRPersistentCacheIndexManager) {
    public actual fun enableIndexAutoCreation() {
        ios.enableIndexAutoCreation()
    }

    public actual fun disableIndexAutoCreation() {
        ios.disableIndexAutoCreation()
    }

    public actual fun deleteAllIndexes() {
        ios.deleteAllIndexes()
    }
}

/**
 * An `NSInputStream` reading a Kotlin [java.io.InputStream], for the iOS SDK's `loadBundleStream:`, which opens it, reads
 * it to the end on its own worker queue and closes it; the Kotlin stream is closed with it. A failing read puts the
 * stream in the error state with the exception as its `streamError`, which the SDK reports as the load's failure. The
 * stream is polled, so the run-loop scheduling and delegate of `NSStream` are no-ops.
 */
@OptIn(UnsafeNumber::class)
private class InputStreamAdapter(private val stream: java.io.InputStream) : NSInputStream(NSData()) {
    private var status = NSStreamStatusNotOpen
    private var error: NSError? = null

    override fun streamStatus() = status

    override fun streamError() = error

    override fun open() {
        if (status == NSStreamStatusNotOpen) status = NSStreamStatusOpen
    }

    override fun close() {
        if (status == NSStreamStatusClosed) return
        status = NSStreamStatusClosed
        try {
            stream.close()
        } catch (_: Exception) {
            // The reader is done with the stream either way.
        }
    }

    override fun read(buffer: CPointer<uint8_tVar>?, maxLength: NSUInteger): NSInteger {
        if (status != NSStreamStatusOpen && status != NSStreamStatusAtEnd || buffer == null) return -1
        val chunk = ByteArray(minOf(maxLength.toLong(), BUNDLE_CHUNK_SIZE.toLong()).toInt())
        val count = try {
            stream.read(chunk)
        } catch (e: Exception) {
            error = e.toNSError()
            status = NSStreamStatusError
            return -1
        }
        if (count < 0) {
            status = NSStreamStatusAtEnd
            return 0
        }
        if (count > 0) chunk.usePinned { memcpy(buffer, it.addressOf(0), count.convert()) }
        return count.convert()
    }

    override fun getBuffer(buffer: CPointer<CPointerVar<uint8_tVar>>?, length: CPointer<NSUIntegerVar>?): Boolean = false

    override fun hasBytesAvailable(): Boolean = status == NSStreamStatusOpen

    override fun propertyForKey(key: NSStreamPropertyKey): Any? = null

    override fun setProperty(property: Any?, forKey: NSStreamPropertyKey): Boolean = false

    override fun scheduleInRunLoop(aRunLoop: NSRunLoop, forMode: NSRunLoopMode) {}

    override fun removeFromRunLoop(aRunLoop: NSRunLoop, forMode: NSRunLoopMode) {}
}

private const val BUNDLE_CHUNK_SIZE = 16 * 1024
