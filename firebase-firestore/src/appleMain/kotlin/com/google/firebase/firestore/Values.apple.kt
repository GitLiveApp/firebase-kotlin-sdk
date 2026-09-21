/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.FIRDocumentReference
import cocoapods.FirebaseFirestoreInternal.FIRFieldPath
import cocoapods.FirebaseFirestoreInternal.FIRFieldValue
import cocoapods.FirebaseFirestoreInternal.FIRFilter
import cocoapods.FirebaseFirestoreInternal.FIRFirestoreErrorDomain
import cocoapods.FirebaseFirestoreInternal.FIRGeoPoint
import cocoapods.FirebaseFirestoreInternal.FIRTimestamp
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.Timestamp
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import platform.Foundation.NSNull
import platform.Foundation.create
import platform.posix.memcpy

/*
 * The iOS SDK reads and writes Foundation values with its own FIRTimestamp, FIRGeoPoint, FIRDocumentReference and NSData;
 * the layer presents them as the Android SDK's types: Map, List, String, Boolean, Long, Double,
 * com.google.firebase.Timestamp, GeoPoint, DocumentReference and Blob.
 */

/** [this] as the iOS SDK writes it: the layer's value types become the SDK's. */
internal fun Any?.toIos(): Any? = when (this) {
    null -> null
    is String, is Boolean, is Number -> this
    is Timestamp -> FIRTimestamp(seconds, nanoseconds)
    is GeoPoint -> FIRGeoPoint(latitude, longitude)
    is DocumentReference -> ios
    is FieldValue -> toIosFieldValue()
    is Blob -> toBytes().toNSData()
    is FieldPath -> toIos()
    is Map<*, *> -> entries.associate { (key, value) -> key.toIos() to value.toIos() }
    is Collection<*> -> map { it.toIos() }
    is Array<*> -> map { it.toIos() }
    else -> this
}

/** [this] as the iOS SDK writes a document: a map of field values. */
@Suppress("UNCHECKED_CAST")
internal fun Any.toIosData(): Map<Any?, *> = toIos() as? Map<Any?, *> ?: throw IllegalArgumentException("Document data must be a Map<String, Any?>, was $this")

/** A value read by the iOS SDK as the Android SDK's types (see [toIos]). */
internal fun Any?.toCompat(): Any? = when (this) {
    null, is NSNull -> null
    is FIRTimestamp -> Timestamp(seconds, nanoseconds)
    is FIRGeoPoint -> GeoPoint(latitude, longitude)
    is FIRDocumentReference -> DocumentReference(this)
    is NSData -> Blob.fromBytes(toByteArray())
    is Map<*, *> -> entries.associate { (key, value) -> key.toString() to value.toCompat() }
    is List<*> -> map { it.toCompat() }
    else -> this
}

@Suppress("UNCHECKED_CAST")
internal fun Map<Any?, *>?.toCompatData(): Map<String, Any?>? = this?.toCompat() as Map<String, Any?>?

private fun FieldValue.toIosFieldValue(): FIRFieldValue = when (operation) {
    FieldValue.Operation.SERVER_TIMESTAMP -> FIRFieldValue.fieldValueForServerTimestamp()
    FieldValue.Operation.DELETE -> FIRFieldValue.fieldValueForDelete()
    FieldValue.Operation.ARRAY_UNION -> FIRFieldValue.fieldValueForArrayUnion(arguments.map { it.toIos() })
    FieldValue.Operation.ARRAY_REMOVE -> FIRFieldValue.fieldValueForArrayRemove(arguments.map { it.toIos() })
    FieldValue.Operation.INCREMENT -> when (val amount = arguments.single()) {
        is Long -> FIRFieldValue.fieldValueForIntegerIncrement(amount)
        else -> FIRFieldValue.fieldValueForDoubleIncrement((amount as Number).toDouble())
    }
}

internal fun FieldPath.toIos(): FIRFieldPath = if (isDocumentId) FIRFieldPath.documentID() else FIRFieldPath(segments)

/** The fields of [this] as the iOS SDK's `mergeFields` takes them; the SDK accepts both names and field paths. */
internal fun SetOptions.iosMergeFields(): List<Any> = mergeFields.orEmpty().map { it.toIos() }

internal fun Filter.Node.toIos(): FIRFilter = when (this) {
    is Filter.Node.Field -> {
        val path = fieldPath.toIos()
        val ios = value.toIos()
        when (operator) {
            Filter.Operator.EQUAL -> FIRFilter.filterWhereFieldPath(path, isEqualTo = ios ?: NSNull.`null`())
            Filter.Operator.NOT_EQUAL -> FIRFilter.filterWhereFieldPath(path, isNotEqualTo = ios ?: NSNull.`null`())
            Filter.Operator.LESS_THAN -> FIRFilter.filterWhereFieldPath(path, isLessThan = ios!!)
            Filter.Operator.LESS_THAN_OR_EQUAL -> FIRFilter.filterWhereFieldPath(path, isLessThanOrEqualTo = ios!!)
            Filter.Operator.GREATER_THAN -> FIRFilter.filterWhereFieldPath(path, isGreaterThan = ios!!)
            Filter.Operator.GREATER_THAN_OR_EQUAL -> FIRFilter.filterWhereFieldPath(path, isGreaterThanOrEqualTo = ios!!)
            Filter.Operator.ARRAY_CONTAINS -> FIRFilter.filterWhereFieldPath(path, arrayContains = ios!!)
            Filter.Operator.ARRAY_CONTAINS_ANY -> FIRFilter.filterWhereFieldPath(path, arrayContainsAny = ios as List<*>)
            Filter.Operator.IN -> FIRFilter.filterWhereFieldPath(path, `in` = ios as List<*>)
            Filter.Operator.NOT_IN -> FIRFilter.filterWhereFieldPath(path, notIn = ios as List<*>)
            else -> throw IllegalArgumentException("$operator is not a field operator")
        }
    }
    is Filter.Node.Composite -> when (operator) {
        Filter.Operator.AND -> FIRFilter.andFilterWithFilters(filters.map { it.toIos() })
        else -> FIRFilter.orFilterWithFilters(filters.map { it.toIos() })
    }
}

internal fun ByteArray.toNSData(): NSData = if (isEmpty()) NSData() else usePinned { NSData.create(bytes = it.addressOf(0), length = size.toULong()) }

internal fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    if (isNotEmpty()) usePinned { memcpy(it.addressOf(0), bytes, length) }
}

/** The iOS SDK's error codes are the gRPC statuses the Android SDK's [FirebaseFirestoreException.Code] values. */
internal fun NSError.toFirestoreException(): FirebaseFirestoreException = FirebaseFirestoreException(
    localizedDescription,
    if (domain == FIRFirestoreErrorDomain) FirebaseFirestoreException.Code.fromValue(code.toInt()) else FirebaseFirestoreException.Code.UNKNOWN,
)

/** [this] as the error the iOS SDK's transaction block reports, so that the SDK aborts the transaction. */
internal fun Throwable.toNSError(): NSError = NSError.errorWithDomain(
    FIRFirestoreErrorDomain,
    ((this as? FirebaseFirestoreException)?.code ?: FirebaseFirestoreException.Code.ABORTED).value().toLong(),
    mapOf<Any?, Any?>(NSLocalizedDescriptionKey to (message ?: toString())),
)

internal inline fun <T> task(crossinline start: ((T, NSError?) -> Unit) -> Unit): Task<T> {
    val source = TaskCompletionSource<T>()
    start { result, error -> if (error == null) source.setResult(result) else source.setException(error.toFirestoreException()) }
    return source.task
}

/** A write as a [Task] of the SDK's completion block. */
internal inline fun write(crossinline start: ((NSError?) -> Unit) -> Unit): Task<Nothing?> = task { completion -> start { error -> completion(null, error) } }
