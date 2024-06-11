/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.*
import cocoapods.FirebaseFirestoreInternal.FIRDocumentChangeType.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.firestore.internal.NativeDocumentSnapshotWrapper
import kotlinx.cinterop.*
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.numberWithLong
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_t

actual val Firebase.firestore get() =
    FirebaseFirestore(FIRFirestore.firestore())

actual fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore = FirebaseFirestore(
    FIRFirestore.firestoreForApp(app.ios as objcnames.classes.FIRApp),
)

val LocalCacheSettings.ios: FIRLocalCacheSettingsProtocol get() = when (this) {
    is LocalCacheSettings.Persistent -> FIRPersistentCacheSettings(NSNumber.numberWithLong(sizeBytes))
    is LocalCacheSettings.Memory -> FIRMemoryCacheSettings(
        when (garbaseCollectorSettings) {
            is MemoryGarbageCollectorSettings.Eager -> FIRMemoryEagerGCSettings()
            is MemoryGarbageCollectorSettings.LRUGC -> FIRMemoryLRUGCSettings(NSNumber.numberWithLong(garbaseCollectorSettings.sizeBytes))
        },
    )
}

actual typealias NativeFirebaseFirestore = FIRFirestore

val FirebaseFirestore.ios get() = native

actual data class FirebaseFirestoreSettings(
    actual val sslEnabled: Boolean,
    actual val host: String,
    actual val cacheSettings: LocalCacheSettings,
    val dispatchQueue: dispatch_queue_t,
) {

    actual companion object {
        actual val CACHE_SIZE_UNLIMITED: Long = -1L
        internal actual val DEFAULT_HOST: String = "firestore.googleapis.com"
        internal actual val MINIMUM_CACHE_BYTES: Long = 1 * 1024 * 1024
        internal actual val DEFAULT_CACHE_SIZE_BYTES: Long = 100 * 1024 * 1024
    }

    actual class Builder(
        actual var sslEnabled: Boolean,
        actual var host: String,
        actual var cacheSettings: LocalCacheSettings,
        var dispatchQueue: dispatch_queue_t,
    ) {

        actual constructor() : this(
            true,
            DEFAULT_HOST,
            persistentCacheSettings { },
            dispatch_get_main_queue(),
        )

        actual constructor(settings: FirebaseFirestoreSettings) : this(
            settings.sslEnabled,
            settings.host,
            settings.cacheSettings,
            settings.dispatchQueue,
        )

        actual fun build(): FirebaseFirestoreSettings = FirebaseFirestoreSettings(sslEnabled, host, cacheSettings, dispatchQueue)
    }

    val ios: FIRFirestoreSettings get() = FIRFirestoreSettings().apply {
        cacheSettings = this@FirebaseFirestoreSettings.cacheSettings.ios
        sslEnabled = this@FirebaseFirestoreSettings.sslEnabled
        host = this@FirebaseFirestoreSettings.host
        dispatchQueue = this@FirebaseFirestoreSettings.dispatchQueue
    }
}

actual fun firestoreSettings(
    settings: FirebaseFirestoreSettings?,
    builder: FirebaseFirestoreSettings.Builder.() -> Unit,
): FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder().apply {
    settings?.let {
        sslEnabled = it.sslEnabled
        host = it.host
        cacheSettings = it.cacheSettings
        dispatchQueue = it.dispatchQueue
    }
}.apply(builder).build()

actual typealias NativeWriteBatch = FIRWriteBatch

val WriteBatch.ios get() = native

actual typealias NativeTransaction = FIRTransaction

val Transaction.ios get() = native

/** A class representing a platform specific Firebase DocumentReference. */
actual typealias NativeDocumentReferenceType = FIRDocumentReference

val DocumentReference.ios get() = native.ios

actual typealias NativeQuery = FIRQuery

val Query.ios get() = native

actual typealias NativeCollectionReference = FIRCollectionReference

val CollectionReference.ios get() = native

actual class FirebaseFirestoreException(message: String, val code: FirestoreExceptionCode) : FirebaseException(message)

actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual enum class FirestoreExceptionCode {
    OK,
    CANCELLED,
    UNKNOWN,
    INVALID_ARGUMENT,
    DEADLINE_EXCEEDED,
    NOT_FOUND,
    ALREADY_EXISTS,
    PERMISSION_DENIED,
    RESOURCE_EXHAUSTED,
    FAILED_PRECONDITION,
    ABORTED,
    OUT_OF_RANGE,
    UNIMPLEMENTED,
    INTERNAL,
    UNAVAILABLE,
    DATA_LOSS,
    UNAUTHENTICATED,
}

actual enum class Direction {
    ASCENDING,
    DESCENDING,
}

actual enum class ChangeType(internal val ios: FIRDocumentChangeType) {
    ADDED(FIRDocumentChangeTypeAdded),
    MODIFIED(FIRDocumentChangeTypeModified),
    REMOVED(FIRDocumentChangeTypeRemoved),
}

fun NSError.toException() = when (domain) {
    FIRFirestoreErrorDomain -> when (code) {
        FIRFirestoreErrorCodeOK -> FirestoreExceptionCode.OK
        FIRFirestoreErrorCodeCancelled -> FirestoreExceptionCode.CANCELLED
        FIRFirestoreErrorCodeUnknown -> FirestoreExceptionCode.UNKNOWN
        FIRFirestoreErrorCodeInvalidArgument -> FirestoreExceptionCode.INVALID_ARGUMENT
        FIRFirestoreErrorCodeDeadlineExceeded -> FirestoreExceptionCode.DEADLINE_EXCEEDED
        FIRFirestoreErrorCodeNotFound -> FirestoreExceptionCode.NOT_FOUND
        FIRFirestoreErrorCodeAlreadyExists -> FirestoreExceptionCode.ALREADY_EXISTS
        FIRFirestoreErrorCodePermissionDenied -> FirestoreExceptionCode.PERMISSION_DENIED
        FIRFirestoreErrorCodeResourceExhausted -> FirestoreExceptionCode.RESOURCE_EXHAUSTED
        FIRFirestoreErrorCodeFailedPrecondition -> FirestoreExceptionCode.FAILED_PRECONDITION
        FIRFirestoreErrorCodeAborted -> FirestoreExceptionCode.ABORTED
        FIRFirestoreErrorCodeOutOfRange -> FirestoreExceptionCode.OUT_OF_RANGE
        FIRFirestoreErrorCodeUnimplemented -> FirestoreExceptionCode.UNIMPLEMENTED
        FIRFirestoreErrorCodeInternal -> FirestoreExceptionCode.INTERNAL
        FIRFirestoreErrorCodeUnavailable -> FirestoreExceptionCode.UNAVAILABLE
        FIRFirestoreErrorCodeDataLoss -> FirestoreExceptionCode.DATA_LOSS
        FIRFirestoreErrorCodeUnauthenticated -> FirestoreExceptionCode.UNAUTHENTICATED
        else -> FirestoreExceptionCode.UNKNOWN
    }
    else -> FirestoreExceptionCode.UNKNOWN
}.let { FirebaseFirestoreException(description!!, it) }

actual class QuerySnapshot(val ios: FIRQuerySnapshot) {
    actual val documents
        get() = ios.documents.map { DocumentSnapshot(NativeDocumentSnapshotWrapper(it as FIRDocumentSnapshot)) }
    actual val documentChanges
        get() = ios.documentChanges.map { DocumentChange(it as FIRDocumentChange) }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(ios.metadata)
}

actual class DocumentChange(val ios: FIRDocumentChange) {
    actual val document: DocumentSnapshot
        get() = DocumentSnapshot(NativeDocumentSnapshotWrapper(ios.document))
    actual val newIndex: Int
        get() = ios.newIndex.toInt()
    actual val oldIndex: Int
        get() = ios.oldIndex.toInt()
    actual val type: ChangeType
        get() = ChangeType.values().first { it.ios == ios.type }
}

actual typealias NativeDocumentSnapshot = FIRDocumentSnapshot

val DocumentSnapshot.ios get() = native

actual class SnapshotMetadata(val ios: FIRSnapshotMetadata) {
    actual val hasPendingWrites: Boolean get() = ios.pendingWrites
    actual val isFromCache: Boolean get() = ios.fromCache
}

actual class FieldPath private constructor(val ios: FIRFieldPath) {
    actual companion object {
        actual val documentId = FieldPath(FIRFieldPath.documentID())
    }
    actual constructor(vararg fieldNames: String) : this(FIRFieldPath(fieldNames.asList()))
    actual val documentId: FieldPath get() = FieldPath.documentId
    actual val encoded: EncodedFieldPath = ios
    override fun equals(other: Any?): Boolean = other is FieldPath && ios == other.ios
    override fun hashCode(): Int = ios.hashCode()
    override fun toString(): String = ios.toString()
}

actual typealias EncodedFieldPath = FIRFieldPath

suspend inline fun <reified T> awaitResult(function: (callback: (T?, NSError?) -> Unit) -> Unit): T {
    val job = CompletableDeferred<T?>()
    function { result, error ->
        if (error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    return job.await() as T
}

suspend inline fun <T> await(function: (callback: (NSError?) -> Unit) -> T): T {
    val job = CompletableDeferred<Unit>()
    val result = function { error ->
        if (error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    job.await()
    return result
}
